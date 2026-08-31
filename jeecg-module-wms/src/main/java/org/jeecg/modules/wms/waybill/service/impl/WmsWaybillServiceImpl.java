package org.jeecg.modules.wms.waybill.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.aspect.annotation.Lock;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.inventory.service.impl.WmsInventoryTransByDeliver;
import org.jeecg.modules.wms.inventory.vo.WmsInventoryTransParam;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersItems;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersItemsService;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersService;
import org.jeecg.modules.wms.shipment.entity.WmsShipment;
import org.jeecg.modules.wms.shipment.entity.WmsShipmentDetail;
import org.jeecg.modules.wms.shipment.mapper.WmsShipmentDetailMapper;
import org.jeecg.modules.wms.shipment.mapper.WmsShipmentMapper;
import org.jeecg.modules.wms.shipment.service.IWmsShipmentDetailService;
import org.jeecg.modules.wms.shipment.service.IWmsShipmentService;
import org.jeecg.modules.wms.shipment.strategy.ShipmentGenerationStrategy;
import org.jeecg.modules.wms.shipment.strategy.ShipmentStrategyFactory;
import org.jeecg.modules.wms.shipment.vo.ShipmentGenerationResult;
import org.jeecg.modules.wms.wave.entity.WmsWaveMaster;
import org.jeecg.modules.wms.wave.service.IWmsWaveMasterService;
import org.jeecg.modules.wms.waybill.service.IWmsSfService;
import org.jeecg.modules.wms.waybill.service.IWmsWaybillService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class WmsWaybillServiceImpl  implements IWmsWaybillService {

	@Autowired
	private IWmsShipmentService wmsShipmentService;

	@Autowired
	private IWmsOutOrdersItemsService wmsOutOrdersItemsService;
	@Autowired
	private IWmsOutOrdersService wmsOutOrdersService;
	@Autowired
	private IWmsTasksRecordsService wmsTasksRecordsService;
	@Autowired
	private WmsWaybillServiceImpl owner;
	@Autowired
	private IWmsSfService sfService;
	@Autowired
	private IWmsProductsService wmsProductsService;
    @Autowired
    private RedissonClient redissonClient;

	@Override
	public void send(String waveId) {

	}

	@Override
	public List<String> selectWaybillNosByWaveId(String waveId) {
		return List.of();
	}

	/**
	 * 请求顺丰生成指定订单的运单
	 * 首先请求顺丰下单，成功后生成运单
	 */
	@Lock(formatter = "ORDERS:CREATEWAYBILL:LOCK:#{orderId}", startDog = true)
	public void generateWaybillForOrder(String orderId) throws Exception {
//		//定义分布式锁名称
//		String lockName = "ORDERS:CREATEWAYBILL:LOCK" + orderId;
//		RLock lock = redissonClient.getLock(lockName);
//		//获取锁
//		boolean b = lock.tryLock(3, -1, TimeUnit.SECONDS);
//		if (b) {//获取锁成功
//			try {
				//查询出库单
				WmsOutOrders order = wmsOutOrdersService.getById(orderId);
				//如果状态不是打包完成，则不能生成运单
				if (!order.getStatus().equals(WarehouseDictEnum.OUTBOUND_PACKED.getCode())) {
					throw new JeecgBootException("出库单" + order.getOrderNo() + "状态不是打包完成，不能生成运单");
				}
				//如果运单已经生成，则不能重复生成
				if (order.getCreatedWaybill().equals("1")) {
					throw new JeecgBootException("出库单" + order.getOrderNo() + "已生成运单，不能重复生成");
				}
				//查询该订单下的包裹
				List<WmsShipment> shipments = wmsShipmentService.selectByOrderId(orderId);

				int shipmentSize = shipments.size();
				if (shipmentSize == 0) {
					throw new JeecgBootException("出库单" + order.getOrderNo() + "没有包裹，不能生成运单");
				}
				//调用顺丰创建运单
				List<String> waybillNos = sfService.requestSfCreateOrder(order, shipments);
				//保存运单号
				owner.saveWaybill(waybillNos, shipments);

//			} finally {
//					//释放锁
//				lock.unlock();
//			}
		}
	}

	/**
	 * 保存运单号
	 * @param waybillNos 运单号
	 * @param shipments 包裹
	 */
	@Transactional(rollbackFor = Exception.class)
	public void saveWaybill(List<String> waybillNos,List<WmsShipment> shipments){
		//如果运单号的数量和包裹数量不一致不能继续，抛出异常
		if(waybillNos.size() != shipments.size()){
			throw new RuntimeException("运单数量和包裹数量不一致");
		}
		//将运单号依次保存到包裹中
		for (int i = 0; i < waybillNos.size(); i++) {
			LambdaUpdateWrapper<WmsShipment> set = new LambdaUpdateWrapper<WmsShipment>()
					.eq(WmsShipment::getId, shipments.get(i).getId())
					.set(WmsShipment::getTrackingNo, waybillNos.get(i));
			wmsShipmentService.update(null,set);
		}
		WmsShipment wmsShipment = shipments.get(0);
		//订单id
		String orderId = wmsShipment.getOrderId();
		//更新订单"createdWaybill"为1
		LambdaUpdateWrapper<WmsOutOrders> update = new LambdaUpdateWrapper<WmsOutOrders>()
				.eq(WmsOutOrders::getId, orderId)
				.set(WmsOutOrders::getCreatedWaybill, "1");
		wmsOutOrdersService.update(null, update);
	}
}