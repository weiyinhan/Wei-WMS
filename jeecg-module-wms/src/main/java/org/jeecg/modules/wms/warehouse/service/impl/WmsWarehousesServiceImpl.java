package org.jeecg.modules.wms.warehouse.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import org.jeecg.modules.wms.warehouse.mapper.WmsWarehousesMapper;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 仓库表
 * @Author: jeecg-boot
 * @Date: 2026-07-11
 * @Version: V1.0
 */
@Service
public class WmsWarehousesServiceImpl extends ServiceImpl<WmsWarehousesMapper, WmsWarehouses>
        implements IWmsWarehousesService {

    /**
     * 添加仓库
     *
     * @param wmsWarehouses
     * @generated
     */
    @Override
    public void add(WmsWarehouses wmsWarehouses) {
        String warehouseCode = wmsWarehouses.getWarehouseCode();
        // 校验仓库代码唯一性
        // 查询数据库
        // select count(1) from wms_warehouses where warehouse_code = ?
        LambdaQueryWrapper<WmsWarehouses> eq = new LambdaQueryWrapper<WmsWarehouses>()
                .eq(WmsWarehouses::getWarehouseCode, warehouseCode);
        long count = this.count(eq);
        if (count > 0) {
            throw new JeecgBootException("仓库代码已存在");
        }
        this.save(wmsWarehouses);
    }

    /**
     * 修改仓库
     *
     * @param wmsWarehouses
     * @generated
     */
    @Override
    public void edit(WmsWarehouses wmsWarehouses) {
        String warehouseCode = wmsWarehouses.getWarehouseCode();
        // 校验仓库代码唯一性
        // 查询数据库
        // select count(1) from wms_warehouses where warehouse_code = ? and id != ?
        LambdaQueryWrapper<WmsWarehouses> eq = new LambdaQueryWrapper<WmsWarehouses>()
                .eq(WmsWarehouses::getWarehouseCode, warehouseCode)
                .ne(WmsWarehouses::getId, wmsWarehouses.getId());
        long count = this.count(eq);
        if (count > 0) {
            throw new JeecgBootException("仓库代码已存在");
        }
        this.updateById(wmsWarehouses);
    }

    @Override
    public void enable(String id) {
        // 通过id查询
        WmsWarehouses wmsWarehouses = this.getById(id);
        // 判断仓库是否存在
        if (wmsWarehouses == null)
            throw new JeecgBootException("仓库不存在");
        // 创建或禁用时才可以使用
        if (!wmsWarehouses.getStatus().equals(WarehouseDictEnum.STATUS_CREATED.getCode()) &&
                !wmsWarehouses.getStatus().equals(WarehouseDictEnum.STATUS_INACTIVE.getCode())) {
            throw new JeecgBootException("仓库为创建或禁用时方可使用");
        }
        // 将仓库值为启用
        wmsWarehouses.setStatus(WarehouseDictEnum.STATUS_ACTIVE.getCode());
        // 更新数据库
        this.updateById(wmsWarehouses);
    }

    @Override
    public void disable(String id) {
        WmsWarehouses wmsWarehouses = this.getById(id);
        //判断仓库是否存在
        if(wmsWarehouses == null)
            throw new JeecgBootException("仓库不存在");
        //在启用时才可以禁用
        if(!wmsWarehouses.getStatus().equals(WarehouseDictEnum.STATUS_ACTIVE.getCode()))
            throw new JeecgBootException("仓库为启用时方可禁用");
        //将仓库值为禁用
        wmsWarehouses.setStatus(WarehouseDictEnum.STATUS_INACTIVE.getCode());
        //更新数据库
        this.updateById(wmsWarehouses);
    }

}
