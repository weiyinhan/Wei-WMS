package org.jeecg.modules.wms.wmstask.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrdersMapper;
import org.jeecg.modules.wms.inorder.service.impl.WmsStockInOrderItemsServiceImpl;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.mapper.WmsTasksMapper;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.jeecg.modules.wms.inorder.service.impl.WmsStockInOrdersServiceImpl;
import org.jeecg.modules.wms.inventory.service.impl.WmsInventoryTransByPutway;
import org.jeecg.modules.wms.inventory.service.impl.WmsInventoryTransByReceiving;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.inventory.vo.WmsInventoryTransParam;

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
    @Autowired
    private IWmsTasksRecordsService wmsTasksRecordsService;
    @Autowired
    private WmsInventoryTransByReceiving wmsInventoryTransByReceiving;
    @Autowired
    private WmsInventoryTransByPutway wmsInventoryTransByPutway;

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

    /**
     * 分页查询任务列表
     * 
     * @param wmsTasks
     * @return
     */
    @Override
    public IPage<WmsTasks> list(WmsTasks wmsTasks, Integer pageNo, Integer pageSize) {
        Page<WmsTasks> page = PageHelper.startPage(pageNo, pageSize);
        List<WmsTasks> list = baseMapper.queryTaskList(wmsTasks);
        PageDTO<WmsTasks> wmsTasksPageDTO = new PageDTO<>();
        wmsTasksPageDTO.setRecords(list);
        wmsTasksPageDTO.setTotal(page.getTotal());
        wmsTasksPageDTO.setSize(page.getPageSize());
        wmsTasksPageDTO.setCurrent(page.getPageNum());
        wmsTasksPageDTO.setPages(page.getPages());
        return wmsTasksPageDTO;
    }

    /**
     * 执行收货任务
     * 
     * @param wmsTasksRecords
     */
    @Override
    public void receive(WmsTasksRecords wmsTasksRecords) {
        // 执行任务
        WmsTasks wmsTasks = execute(wmsTasksRecords);
        // 入库单明细id
        String stockInOrderItemId = wmsTasks.getStockInOrderItemId();
        // 更新入库单明细收货数量，不良品数量及状态
        wmsStockInOrderItemsService.updateReceivedStatus(stockInOrderItemId);
        // 更新入库单总收货数量，不良品数量及状态
        wmsStockInOrdersService.updateReceivedStatus(wmsTasks.getStockInOrderId());
        // 存储库存
        WmsInventoryTransParam wmsInventoryTransParam = new WmsInventoryTransParam();
        // 商品id
        wmsInventoryTransParam.setProductId(wmsTasks.getProductId());
        // 执行数量
        wmsInventoryTransParam.setExecQuantity(wmsTasksRecords.getExecQuantity());
        // 仓库id
        wmsInventoryTransParam.setWarehouseId(wmsTasks.getTargetWarehouseId());
        // 来源储位编码
        wmsInventoryTransParam.setSourceLocationCode(wmsTasksRecords.getSourceLocationCode());
        // 目的储位编码
        wmsInventoryTransParam.setTargetLocationCode(wmsTasksRecords.getTargetLocationCode());
        // 批号
        wmsInventoryTransParam.setBatchNumber(wmsTasksRecords.getBatchNumber());
        wmsInventoryTransParam.setExpiryDate(wmsTasksRecords.getExpiryDate());
        // 数据变更类型
        wmsInventoryTransParam.setTransactionType(WarehouseDictEnum.INVENTORY_RECEIVING.getCode());
        // 执行人
        wmsInventoryTransParam.setOperator(wmsTasks.getOperator());
        // 执行时间
        wmsInventoryTransParam.setOperationTime(new Date());
        wmsInventoryTransByReceiving.transfer(wmsInventoryTransParam);
        // 收货完成后自动创建上架任务
        createPutawayTask(wmsTasks, wmsTasksRecords);

    }

    /**
     * 更新任务
     * 
     * @param wmsTasksRecords
     * @return
     */
    @Override
    public WmsTasks execute(WmsTasksRecords wmsTasksRecords) {
        // 任务id
        String taskId = wmsTasksRecords.getTaskId();
        // 查wmsTasks
        WmsTasks wmsTasks = this.getById(taskId);
        // 判断非空
        if (wmsTasks == null) {
            throw new RuntimeException("任务不存在");
        }
        // 查总数
        Integer quantity = wmsTasks.getQuantity();
        // 完成数量
        Integer completedQuantity = wmsTasks.getCompletedQuantity();
        // 执行数量
        Integer execQuantity = wmsTasksRecords.getExecQuantity();
        // 总数不可大于执行数
        if (completedQuantity + execQuantity > quantity) {
            throw new RuntimeException("本次的执行数量过大");
        }
        // 向任务记录表填充数据
        // 仓库id
        wmsTasksRecords.setTargetWarehouseId(wmsTasks.getTargetWarehouseId());
        // 入库单id
        wmsTasksRecords.setStockInOrderId(wmsTasks.getStockInOrderId());
        // 入库单明细id
        wmsTasksRecords.setStockInOrderItemId(wmsTasks.getStockInOrderItemId());
        // 波次id 用于波次管理
        wmsTasksRecords.setWaveOrderId(wmsTasks.getWaveOrderId());
        // 波次拣货明细id 用于波次管理
        wmsTasksRecords.setWaveSkuSummaryId(wmsTasks.getWaveSkuSummaryId());
        // 任务id
        wmsTasksRecords.setTaskId(wmsTasks.getId());
        // 商品 id
        wmsTasksRecords.setProductId(wmsTasks.getProductId());
        // 任务编码
        wmsTasksRecords.setTaskNumber(wmsTasks.getTaskNumber());
        // 任务类型
        wmsTasksRecords.setTaskType(wmsTasks.getTaskType());
        // 执行时间
        wmsTasksRecords.setOperationTime(new Date());
        // 执行人
        wmsTasksRecords.setOperator(wmsTasks.getOperator());
        // 添加执行任务记录
        boolean save = wmsTasksRecordsService.save(wmsTasksRecords);
        if (!save) {
            throw new RuntimeException("保存失败");
        }

        // 项任务表更新完成收货数量
        LambdaUpdateWrapper<WmsTasks> wmsTasksLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        wmsTasksLambdaUpdateWrapper
                .setSql("completed_quantity = completed_quantity + " + wmsTasksRecords.getExecQuantity())
                .eq(WmsTasks::getId, taskId)
                .le(WmsTasks::getCompletedQuantity, wmsTasks.getQuantity() - wmsTasksRecords.getExecQuantity());
        int update = getBaseMapper().update(null, wmsTasksLambdaUpdateWrapper);
        if (update <= 0) {
            throw new JeecgBootException("执行数量不能大于计划数量!");
        }
        // 重新查询最新数据
        wmsTasks = this.getById(taskId);
        // 最新查询数据
        completedQuantity = wmsTasks.getCompletedQuantity();
        if (completedQuantity >= quantity) {
            // 任务完成
            wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode());
            this.updateById(wmsTasks);
        }
        return wmsTasks;
    }

    /**
     * 创建上架任务
     *
     * @param receiveTask   收货任务
     * @param receiveRecord 收货记录
     */
    private void createPutawayTask(WmsTasks receiveTask, WmsTasksRecords receiveRecord) {
        WmsTasks putawayTask = new WmsTasks();
        putawayTask.setTaskType(WarehouseDictEnum.TASK_TYPE_PUTAWAY.getCode());
        putawayTask.setTaskStatus(WarehouseDictEnum.TASK_STATUS_CREATED.getCode());
        putawayTask.setTaskNumber(generateTaskNumber());
        putawayTask.setProductId(receiveTask.getProductId());
        putawayTask.setQuantity(receiveRecord.getExecQuantity());
        putawayTask.setCompletedQuantity(0);
        // 来源储位：收货时放置的暂存区储位
        putawayTask.setSourceLocationCode(receiveRecord.getTargetLocationCode());
        // 来源容器：收货时使用的容器
        putawayTask.setSourceContainerCode(receiveRecord.getTargetContainerCode());
        putawayTask.setSourceWarehouseId(receiveTask.getTargetWarehouseId());
        putawayTask.setTargetWarehouseId(receiveTask.getTargetWarehouseId());
        putawayTask.setBatchNumber(receiveRecord.getBatchNumber());
        putawayTask.setExpiryDate(receiveRecord.getExpiryDate());
        putawayTask.setStockInOrderId(receiveTask.getStockInOrderId());
        putawayTask.setStockInOrderItemId(receiveTask.getStockInOrderItemId());
        putawayTask.setOperator(receiveTask.getOperator());
        putawayTask.setOperationTime(new Date());
        save(putawayTask);
    }

    /**
     * 执行上架
     *
     * @param wmsTasksRecords
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void putaway(WmsTasksRecords wmsTasksRecords) {
        // 任务id
        String taskId = wmsTasksRecords.getTaskId();
        // 查询上架任务
        WmsTasks wmsTasks = this.getById(taskId);
        if (wmsTasks == null) {
            throw new RuntimeException("任务不存在");
        }
        // 校验任务类型
        if (!WarehouseDictEnum.TASK_TYPE_PUTAWAY.getCode().equals(wmsTasks.getTaskType())) {
            throw new RuntimeException("不是上架任务");
        }
        // 校验任务状态，已完成则不可再上架
        if (WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode().equals(wmsTasks.getTaskStatus())) {
            throw new RuntimeException("任务已完成");
        }
        // 校验上架数量
        Integer quantity = wmsTasks.getQuantity();
        Integer completedQuantity = wmsTasks.getCompletedQuantity();
        Integer execQuantity = wmsTasksRecords.getExecQuantity();
        if (completedQuantity + execQuantity > quantity) {
            throw new RuntimeException("本次上架数量过大");
        }

        // 保存上架记录
        wmsTasksRecords.setProductId(wmsTasks.getProductId());
        wmsTasksRecords.setTaskNumber(wmsTasks.getTaskNumber());
        wmsTasksRecords.setTaskType(wmsTasks.getTaskType());
        wmsTasksRecords.setSourceLocationCode(wmsTasks.getSourceLocationCode());
        wmsTasksRecords.setSourceContainerCode(wmsTasks.getSourceContainerCode());
        wmsTasksRecords.setSourceWarehouseId(wmsTasks.getSourceWarehouseId());
        wmsTasksRecords.setTargetWarehouseId(wmsTasks.getTargetWarehouseId());
        wmsTasksRecords.setBatchNumber(wmsTasks.getBatchNumber());
        wmsTasksRecords.setExpiryDate(wmsTasks.getExpiryDate());
        wmsTasksRecords.setStockInOrderId(wmsTasks.getStockInOrderId());
        wmsTasksRecords.setStockInOrderItemId(wmsTasks.getStockInOrderItemId());
        wmsTasksRecords.setOperationTime(new Date());
        wmsTasksRecords.setOperator(wmsTasks.getOperator());
        wmsTasksRecordsService.save(wmsTasksRecords);

        // 更新任务完成数量和状态
        boolean completed = (completedQuantity + execQuantity) >= quantity;
        LambdaUpdateWrapper<WmsTasks> updateWrapper = new LambdaUpdateWrapper<WmsTasks>()
                .eq(WmsTasks::getId, taskId)
                .setSql("completed_quantity = completed_quantity + " + execQuantity)
                .set(WmsTasks::getTaskStatus,
                        completed ? WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode()
                                : WarehouseDictEnum.TASK_STATUS_EXECUTING.getCode());
        if (completed) {
            updateWrapper.set(WmsTasks::getCompletedAt, new Date());
        }
        update(updateWrapper);

        // 执行库存转移：从来源储位到目标储位
        WmsInventoryTransParam transParam = new WmsInventoryTransParam();
        transParam.setProductId(wmsTasks.getProductId());
        transParam.setExecQuantity(execQuantity);
        transParam.setSourceLocationCode(wmsTasks.getSourceLocationCode());
        transParam.setTargetLocationCode(wmsTasksRecords.getTargetLocationCode());
        transParam.setBatchNumber(wmsTasks.getBatchNumber());
        transParam.setTransactionType(WarehouseDictEnum.INVENTORY_PUTAWAY.getCode());
        transParam.setOperationTime(new Date());
        transParam.setOperator(wmsTasks.getOperator());
        wmsInventoryTransByPutway.transfer(transParam);
    }
}
