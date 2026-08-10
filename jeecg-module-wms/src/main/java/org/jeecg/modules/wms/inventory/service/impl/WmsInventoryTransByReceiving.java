package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.inventory.entity.WmsInventoryTrans;
import org.jeecg.modules.wms.inventory.mapper.WmsInventoryTransMapper;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryTransService;
import org.jeecg.modules.wms.vo.WmsInventoryTransParam;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageLocationsService;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageZonesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WmsInventoryTransByReceiving extends ServiceImpl<WmsInventoryTransMapper, WmsInventoryTrans> implements IWmsInventoryTransService {

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    //注入储位表service
    @Autowired
    private IWmsStorageLocationsService wmsStorageLocationsService;

    //注入储区service
    @Autowired
    private IWmsStorageZonesService wmsStorageZonesService;

    //注入商品service
    @Autowired
    private IWmsProductsService wmsProductsService;

    /**
     * 库存操作
     * @param inventoryTransParam
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(WmsInventoryTransParam inventoryTransParam) {
        //判断非空,商品id,仓库id,醋味编码，是否可售
        if(StringUtils.isEmpty(inventoryTransParam.getProductId())
                ||StringUtils.isEmpty(inventoryTransParam.getWarehouseId())
                ||StringUtils.isEmpty(inventoryTransParam.getSourceLocationCode())
                ||StringUtils.isEmpty(inventoryTransParam.getIsSellable())){
            throw new RuntimeException("商品id,仓库id,储位编码 是否可售不可为空");
        }
        //根据inventoryTransParam.getTargetLocationCode查询储位
        WmsStorageLocations wmsTargetLocation=wmsStorageLocationsService.getOne(new LambdaQueryWrapper<WmsStorageLocations>()
                .eq(WmsStorageLocations::getLocationCode,inventoryTransParam.getTargetLocationCode()));
        if(wmsTargetLocation == null){
            throw new RuntimeException("目标储位编码不存在");
        }
        //查商品
        WmsProducts wmsProducts=wmsProductsService.getById(inventoryTransParam.getProductId());
        if(wmsProducts == null){
            throw new RuntimeException("商品不存在");
        }
        //查询库存
        WmsInventory inventoryByUniqueKey=wmsInventoryService.getInventoryByUniqueKey(inventoryTransParam.getProductId(), inventoryTransParam.getSourceLocationCode(),inventoryTransParam.getBatchNumber());
        //是否可售1/0
        String isSellable=inventoryTransParam.getIsSellable();
        //不存在就创建一条记录
        if(inventoryByUniqueKey == null){
            WmsInventory inventory = new WmsInventory();
            BeanUtils.copyProperties(inventoryTransParam,inventory);
            inventory.setLocationCode(inventoryTransParam.getTargetLocationCode());
            //在库数量
            inventory.setStockQuantity(inventoryTransParam.getExecQuantity());
            //分配数量为零
            inventory.setAllocatedQuantity(0);
            //如果可售设置可用数量
            if(isSellable.equals("1")){
                inventory.setAvailableQuantity(inventoryTransParam.getExecQuantity());
            }
            else{
                inventory.setAvailableQuantity(0);
            }
            //传货主id
            inventory.setOwnerId(wmsProducts.getOwnerId());
            //保存库存
            boolean save = wmsInventoryService.save(inventory);
            if(!save){
                throw new RuntimeException("库存操作失败");
            }   
        }
        //存在就直接加
        else{
            //在原有库存中增加库存
            //如果是良品还要增加可用数量
            new LambdaUpdateWrapper<WmsInventory>()
                    .eq(WmsInventory::getId,inventoryByUniqueKey.getId())
                    .setSql("stock_quantity=stock_quantity+"+inventoryTransParam.getExecQuantity())
                    .setSql(isSellable.equals("1"),"available_quantity=available_quantity+"+inventoryTransParam.getExecQuantity());
                    boolean update = wmsInventoryService.update(null, new LambdaUpdateWrapper<WmsInventory>()
                            .eq(WmsInventory::getId,inventoryByUniqueKey.getId()));
                    if(!update){
                        throw new RuntimeException("库存操作失败");
                    }

        }
    }

}