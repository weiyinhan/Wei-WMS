package org.jeecg.modules.wms.inorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrderItemsMapper;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.service.impl.WmsTasksRecordsServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 入库单明细
 * @Author: jeecg-boot
 * @Date:   2026-07-20
 * @Version: V1.0
 */
@Service
public class WmsStockInOrderItemsServiceImpl extends ServiceImpl<WmsStockInOrderItemsMapper, WmsStockInOrderItems> implements IWmsStockInOrderItemsService {
	
	@Autowired
	private WmsStockInOrderItemsMapper wmsStockInOrderItemsMapper;

	/**
	 * 收货记录
	 * @param
	 * @return
	 */
	@Autowired
	private WmsTasksRecordsServiceImpl wmsTasksRecordsService;
	
	@Override
	public List<WmsStockInOrderItems> selectByMainId(String mainId) {
		return wmsStockInOrderItemsMapper.selectByMainId(mainId);
	}

	@Override
	public void updateReceivedStatus(String stockInOrderItemId) {
	 	//查询入库单明细
		WmsStockInOrderItems stockInOrderItemUpdate = getById(stockInOrderItemId);

 		//根据入库单明细id查询任务记录表
		//查询入库单收了多少良品不良品
		LambdaQueryWrapper<WmsTasksRecords> eq = new LambdaQueryWrapper<WmsTasksRecords>()
				.eq(WmsTasksRecords::getStockInOrderItemId,stockInOrderItemId);
		List<WmsTasksRecords> wmsTasksRecords = wmsTasksRecordsService.list(eq);
		//拿到不良品
		List<WmsTasksRecords> collect = wmsTasksRecords.stream().filter(item->item.getInventoryAttribute()
		.equals(WarehouseDictEnum.INVENTORY_ATTRIBUTE_DEFECTIVE.getCode())).toList();
		//汇总wmsTasksRecords中的不良品，根据属性过滤
		int badQuantity = collect.stream().mapToInt(WmsTasksRecords::getExecQuantity).sum();

		//拿到良品
		int goodQuantity= wmsTasksRecords.stream()
		.filter(item->item.getInventoryAttribute()
		.equals(WarehouseDictEnum.INVENTORY_ATTRIBUTE_GOOD.getCode()))
		.mapToInt(WmsTasksRecords::getExecQuantity)
		.sum();

		//更新
		WmsStockInOrderItems wmsStockInOrderItems = new WmsStockInOrderItems();
		wmsStockInOrderItems.setId(stockInOrderItemId);
		wmsStockInOrderItems.setReceivedQuantity(goodQuantity);
		wmsStockInOrderItems.setDefectiveQuantity(badQuantity);
		wmsStockInOrderItemsMapper.updateById(wmsStockInOrderItems);
	}
	
}
