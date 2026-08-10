package org.jeecg.modules.wms.inorder.service.impl;

import com.alibaba.fastjson2.stream.StreamReader;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrderItemsMapper;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrdersMapper;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrdersService;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import org.jeecg.common.util.DateUtils;
import java.util.Date;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static net.sf.jsqlparser.parser.feature.Feature.insert;
import static net.sf.jsqlparser.parser.feature.Feature.update;

/**
 * @Description: 入库单主表
 * @Author: jeecg-boot
 * @Date: 2026-07-20
 * @Version: V1.0
 */
@Service
public class WmsStockInOrdersServiceImpl extends ServiceImpl<WmsStockInOrdersMapper, WmsStockInOrders>
		implements IWmsStockInOrdersService {

	@Autowired
	private WmsStockInOrdersMapper wmsStockInOrdersMapper;
	@Autowired
	private WmsStockInOrderItemsMapper wmsStockInOrderItemsMapper;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private IWmsStockInOrderItemsService wmsStockInOrderItemsService;


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(WmsStockInOrders wmsStockInOrders, List<WmsStockInOrderItems> wmsStockInOrderItemsList) {
		wmsStockInOrdersMapper.insert(wmsStockInOrders);
		if (wmsStockInOrderItemsList != null && wmsStockInOrderItemsList.size() > 0) {
			for (WmsStockInOrderItems entity : wmsStockInOrderItemsList) {
				entity.setOrderId(wmsStockInOrders.getId());
				wmsStockInOrderItemsMapper.insert(entity);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(WmsStockInOrders wmsStockInOrders,
			List<WmsStockInOrderItems> wmsStockInOrderItemsList) {
		wmsStockInOrdersMapper.updateById(wmsStockInOrders);
		WmsStockInOrders old = wmsStockInOrdersMapper.selectById(wmsStockInOrders.getId());
		if (old == null) {
			throw new JeecgBootException("入库单不可以为空");
		}
		if (wmsStockInOrderItemsList == null || wmsStockInOrderItemsList.size() < 1) {
			throw new JeecgBootException("入库单明细不能为空");
		}
		if (!(WarehouseDictEnum.INBOUND_INITIAL.getCode().equals(old.getStatus())
				|| WarehouseDictEnum.INBOUND_REJECTED.getCode().equals(old.getStatus()))) {
			throw new JeecgBootException("只有初始状态和审核失败的入库单才可以修改");
		}
		wmsStockInOrdersMapper.updateById(wmsStockInOrders);
		wmsStockInOrderItemsMapper.deleteByMainId(wmsStockInOrders.getId());
		Map<String, List<WmsStockInOrderItems>> collect = wmsStockInOrderItemsList.stream()
				.collect(Collectors.groupingBy(WmsStockInOrderItems::getProductId));
		List<WmsStockInOrderItems> mergeList = new java.util.ArrayList<>();
		for (Map.Entry<String, List<WmsStockInOrderItems>> entry : collect.entrySet()) {
			List<WmsStockInOrderItems> list = entry.getValue();
			WmsStockInOrderItems wmsStockInOrderItems = list.get(0);
			if (list.size() > 1) {
				Integer sumQuantity = list.stream().mapToInt(WmsStockInOrderItems::getExpectedQuantity).sum();
				wmsStockInOrderItems.setExpectedQuantity(sumQuantity);
			}
			wmsStockInOrderItems.setOrderId(wmsStockInOrders.getId());
			mergeList.add(wmsStockInOrderItems);
		}
		wmsStockInOrderItemsService.saveBatch(mergeList);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		wmsStockInOrderItemsMapper.deleteByMainId(id);
		wmsStockInOrdersMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for (Serializable id : idList) {
			wmsStockInOrderItemsMapper.deleteByMainId(id.toString());
			wmsStockInOrdersMapper.deleteById(id);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void add(WmsStockInOrders wmsStockInOrders) {
		String orderNumber = generateOrderNumber(wmsStockInOrders);
		wmsStockInOrders.setStatus(WarehouseDictEnum.INBOUND_INITIAL.getCode());
		wmsStockInOrders.setOrderNumber(orderNumber);
		int insert = wmsStockInOrdersMapper.insert(wmsStockInOrders);
		if (insert < 1) {
			throw new JeecgBootException("生成入库单编号失败");
		}
	}

	public String generateOrderNumber(WmsStockInOrders wmsStockInOrders) {
		String now = DateUtils.now().substring(0, 10).replaceAll("-", "");
		String key = "wms:asn_number" + now;
		long incr = 0;
		try {
			incr = redisUtil.incr(key, 1);
			if (incr == 1) {
				redisUtil.expire(key, 60 * 60 * 24 + 10);
			}
		} catch (Exception e) {
			throw new RuntimeException("生成入库单编号失败");
		}
		String orderNumber = "ASN" + now + String.format("%04d", incr);
		return orderNumber;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void submitAudit(String id) {
		WmsStockInOrders wmsStockInOrders = getById(id);
		if (wmsStockInOrders == null) {
			throw new JeecgBootException("未找到对应入库单");
		}
		if (!wmsStockInOrders.getStatus().equals(WarehouseDictEnum.INBOUND_INITIAL.getCode())) {
			throw new JeecgBootException("只有初始状态的入库单才能提交审核");
		}
		wmsStockInOrders.setStatus(WarehouseDictEnum.INBOUND_SUBMIT_AUDIT.getCode());
		updateById(wmsStockInOrders);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void auditPass(String id) {
		WmsStockInOrders wmsStockInOrders = getById(id);
		if (wmsStockInOrders == null) {
			throw new JeecgBootException("未找到对应入库单");
		}
		if (!wmsStockInOrders.getStatus().equals(WarehouseDictEnum.INBOUND_SUBMIT_AUDIT.getCode())) {
			throw new JeecgBootException("只有提交审核状态的入库单才能审核通过");
		}
		wmsStockInOrders.setStatus(WarehouseDictEnum.INBOUND_APPROVED.getCode());
		updateById(wmsStockInOrders);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void auditReject(String id) {
		WmsStockInOrders wmsStockInOrders = getById(id);
		if (wmsStockInOrders == null) {
			throw new JeecgBootException("未找到对应入库单");
		}
		if (!wmsStockInOrders.getStatus().equals(WarehouseDictEnum.INBOUND_SUBMIT_AUDIT.getCode())) {
			throw new JeecgBootException("只有提交审核状态的入库单才能审核不通过");
		}
		wmsStockInOrders.setStatus(WarehouseDictEnum.INBOUND_REJECTED.getCode());
		updateById(wmsStockInOrders);
	}

	@Override
	public String updateReceivedStatus(String stockInOrderId) {
		WmsStockInOrders wmsStockInOrders = new WmsStockInOrders();
		//根据入库单id查明细
		List<WmsStockInOrderItems> wmsStockInOrderItems = wmsStockInOrderItemsService.selectByMainId(stockInOrderId);
		//统计明细中的收货总数和不良品总数
		int goodQuantity = wmsStockInOrderItems.stream()
				.mapToInt(WmsStockInOrderItems::getReceivedQuantity)
				.sum();
		int badQuantity = wmsStockInOrderItems.stream()
				.mapToInt(WmsStockInOrderItems::getDefectiveQuantity)
				.sum();
		wmsStockInOrders.setTotalReceivedQuantity(goodQuantity);
		wmsStockInOrders.setTotalDefectiveQuantity(badQuantity);
		//判断是否全部收货完成：没有任何明细处于"收货中"状态
		boolean allReceived = wmsStockInOrderItems.stream()
				.noneMatch(item -> WarehouseDictEnum.INBOUND_RECEIVING.getCode().equals(item.getStatus()));
		if (allReceived) {
			//全部收货完成
			wmsStockInOrders.setStatus(WarehouseDictEnum.INBOUND_RECEIVED.getCode());
		} else {
			//还在收货中
			wmsStockInOrders.setStatus(WarehouseDictEnum.INBOUND_RECEIVING.getCode());
		}
		updateById(wmsStockInOrders);
		return wmsStockInOrders.getStatus();
	}
}