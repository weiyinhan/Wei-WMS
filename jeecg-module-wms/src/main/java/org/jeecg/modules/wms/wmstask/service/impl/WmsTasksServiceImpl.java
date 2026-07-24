package org.jeecg.modules.wms.wmstask.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrdersMapper;
import org.jeecg.modules.wms.inorder.service.impl.WmsStockInOrderItemsServiceImpl;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.mapper.WmsTasksMapper;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.jeecg.modules.wms.inorder.service.impl.WmsStockInOrdersServiceImpl;
import org.jeecg.common.util.RedisUtil;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description: 任务表
 * @Author: jeecg-boot
 * @Date: 2026-07-23
 * @Version: V1.0
 */
@Service
public class WmsTasksServiceImpl extends ServiceImpl<WmsTasksMapper, WmsTasks> implements IWmsTasksService {

    @Autowired
    private WmsStockInOrdersServiceImpl wmsStockInOrdersService;
    @Autowired
    private WmsStockInOrderItemsServiceImpl wmsStockInOrderItemsService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReceiveTask(String orderId, String operator) {
        // 查入库单
        WmsStockInOrders wmsStockInOrders = wmsStockInOrdersService.getById(orderId);
        // 要求审核通过才可以创建收货任务
        if (!wmsStockInOrders.getStatus().equals(WarehouseDictEnum.INBOUND_APPROVED.getCode())) {
            throw new JeecgBootException("入库单要求审核通过才可以创建收货任务");
        }
        // 查询入库单明细
        List<WmsStockInOrderItems> wmsStockInOrderItems = wmsStockInOrderItemsService.selectByMainId(orderId);
        // 遍历入库单明细，创建收货任务
        for (WmsStockInOrderItems item : wmsStockInOrderItems) {
            // 创建收货任务
            WmsTasks wmsTasks = new WmsTasks();
            // 任务类型,收货任务
            wmsTasks.setTaskType(WarehouseDictEnum.TASK_TYPE_RECEIVING.getCode());
            // 任务状态，已创建
            wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_CREATED.getCode());
            // 任务创建时间
            wmsTasks.setCreateTime(new Date());
            // 任务号
            wmsTasks.setTaskNumber(generateTaskNumber());
            // 目的仓库
            wmsTasks.setTargetWarehouseId(wmsStockInOrders.getWarehouseId());
            // 入库单id
            wmsTasks.setStockInOrderId(item.getOrderId());
            // 入库单明细id
            wmsTasks.setStockInOrderItemId(item.getId());
            // 商品id
            wmsTasks.setProductId(item.getProductId());
            // 商品采购数量
            wmsTasks.setQuantity(item.getExpectedQuantity());
            // 完成数量为0
            wmsTasks.setCompletedQuantity(0);
            // 执行人
            wmsTasks.setOperator(operator);
            // 创建任务
            save(wmsTasks);
        }
        // 更新入库单状态为收货中，更新入库单明细状态为收货中
        WmsStockInOrders StockInOrdersUpdate = new WmsStockInOrders();
        StockInOrdersUpdate.setId(orderId);
        StockInOrdersUpdate.setStatus(WarehouseDictEnum.INBOUND_RECEIVING.getCode());
        wmsStockInOrdersService.updateById(StockInOrdersUpdate);
        // 更新入库单明细为收货中
        LambdaUpdateWrapper<WmsStockInOrderItems> updateWrapper = new LambdaUpdateWrapper<WmsStockInOrderItems>()
                .eq(WmsStockInOrderItems::getOrderId, orderId)
                .set(WmsStockInOrderItems::getStatus, WarehouseDictEnum.INBOUND_RECEIVING.getCode());
        wmsStockInOrderItemsService.update(updateWrapper);
    }

    /**
     * 生成入库单号
     * 
     * @return
     */
    public String generateTaskNumber() {
        String time = DateUtils.now().substring(0, 10).replaceAll("-", "");
        String key = "wms:task_number" + time;
        long incr = 0;
        incr = redisUtil.incr(key, 1);
        if (incr == 1) {
            redisUtil.expire(key, 60 * 60 * 24 + 10);
        }
        // 将incr组位字符串
        String incrStr = String.format("%05d", incr);
        String taskNumber = "TASK" + time + incrStr;
        return taskNumber;
    }

}
