package org.jeecg.modules.wms.inorder.service;

import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import com.baomidou.mybatisplus.extension.service.IService;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 入库单主表
 * @Author: jeecg-boot
 * @Date: 2026-07-20
 * @Version: V1.0
 */
public interface IWmsStockInOrdersService extends IService<WmsStockInOrders> {

	/**
	 * 添加一对多
	 *
	 * @param wmsStockInOrders
	 * @param wmsStockInOrderItemsList
	 */
	public void saveMain(WmsStockInOrders wmsStockInOrders, List<WmsStockInOrderItems> wmsStockInOrderItemsList);

	/**
	 * 修改一对多
	 *
	 * @param wmsStockInOrders
	 * @param wmsStockInOrderItemsList
	 */
	public void updateMain(WmsStockInOrders wmsStockInOrders, List<WmsStockInOrderItems> wmsStockInOrderItemsList);

	/**
	 * 删除一对多
	 *
	 * @param id
	 */
	public void delMain(String id);

	/**
	 * 批量删除一对多
	 *
	 * @param idList
	 */
	public void delBatchMain(Collection<? extends Serializable> idList);

	void add(WmsStockInOrders wmsStockInOrders);

	String generateOrderNumber(WmsStockInOrders wmsStockInOrders);

//提交审核
	void submitAudit(String id);
//通过
	void auditPass(String id);
//拒绝
	void auditReject(String id);
	//更新入库单明细表
	String updateReceivedStatus(String stockInOrderId);
}
