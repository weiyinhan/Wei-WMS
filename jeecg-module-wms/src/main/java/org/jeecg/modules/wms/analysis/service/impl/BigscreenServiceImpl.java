/**
 * @author Mr.M
 * @version 1.0
 * @description 首页大屏接口实现类
 * @date 2025/8/13 19:03
 */
package org.jeecg.modules.wms.analysis.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.wms.analysis.mapper.BigscreenMapper;
import org.jeecg.modules.wms.analysis.service.BigscreenService;
import org.jeecg.modules.wms.analysis.vo.TodoTask;
import org.jeecg.modules.wms.analysis.vo.TrendVo;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description 首页大屏接口实现类
 * @date 2025/8/13 19:03
 */

@Service
@Slf4j
public class BigscreenServiceImpl implements BigscreenService {

    @Autowired
    private BigscreenMapper bigscreenMapper;

    @Override
    public List<TodoTask> findTodoTaskList(String warehouseId) {
        // 所有待收货任务数量
        int totalReceivingTaskCount = 0;
        // 所以待上架任务数量
        int totalPutawayTaskCount = 0;
        // 所有待拣货任务数量
        int totalPickingTaskCount = 0;
        // 打包任务数量
        int totalPackingTaskCount = 0;

        // 已完成收货任务数量
        int completedReceivingTaskCount = 0;
        // 已完成上架任务数量
        int completedPutawayTaskCount = 0;
        // 已完成拣货任务数量
        int completedPickingTaskCount = 0;
        // 已完成打包任务数量
        int completedPackingTaskCount = 0;
        // 当前时间(年-月-日)
        String date = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
        WmsTasks wmsTasks = new WmsTasks();
        // wmsTasks.setCreateTimeString( date);
        // 仓库id
        wmsTasks.setTargetWarehouseId(warehouseId);
        List<WmsTasks> allTodoTaskList = bigscreenMapper.countTaskList(wmsTasks);
        for (WmsTasks task : allTodoTaskList) {
            switch (task.getTaskType()) {
                case "RECEIVING_TASK":
                    totalReceivingTaskCount = task.getTaskCount();
                    break;
                case "PUTAWAY_TASK":
                    totalPutawayTaskCount = task.getTaskCount();
                    break;
                case "PICKING_TASK":
                    totalPickingTaskCount = task.getTaskCount();
                    break;
                case "PACKING_TASK":
                    totalPackingTaskCount = task.getTaskCount();
                    break;
            }
        }

        wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode());
        // 统计已完成的任务
        List<WmsTasks> completedTodoTaskList = bigscreenMapper.countTaskList(wmsTasks);
        for (WmsTasks task : completedTodoTaskList) {
            switch (task.getTaskType()) {
                case "RECEIVING_TASK":
                    completedReceivingTaskCount = task.getTaskCount();
                    break;
                case "PUTAWAY_TASK":
                    completedPutawayTaskCount = task.getTaskCount();
                    break;
                case "PICKING_TASK":
                    completedPickingTaskCount = task.getTaskCount();
                    break;
                case "PACKING_TASK":
                    completedPackingTaskCount = task.getTaskCount();
                    break;
            }
        }

        // 待收货任务数量
        int todoReceivingTaskCount = totalReceivingTaskCount - completedReceivingTaskCount;
        // 待上架任务数量
        int todoPutawayTaskCount = totalPutawayTaskCount - completedPutawayTaskCount;
        // 待拣货任务数量
        int todoPickingTaskCount = totalPickingTaskCount - completedPickingTaskCount;
        // 待打包任务数量
        int todoPackingTaskCount = totalPackingTaskCount - completedPackingTaskCount;

        /**
         * 参考下边的静态数据构建List<TodoTask>
         * new TodoTask("待收货任务","visit-count|svg",10,5,"当天"),
         * new TodoTask("待上架任务","total-sales|svg",10,5,"当天"),
         * new TodoTask("待审核任务","download-count|svg",10,5,"当天")
         */

        List<TodoTask> todoTaskList = new ArrayList<>();
        todoTaskList.add(
                new TodoTask("待收货任务", "visit-count|svg", todoReceivingTaskCount, completedReceivingTaskCount, "当天"));
        todoTaskList
                .add(new TodoTask("待上架任务", "total-sales|svg", todoPutawayTaskCount, completedPutawayTaskCount, "当天"));
        todoTaskList.add(
                new TodoTask("待拣货任务", "download-count|svg", todoPickingTaskCount, completedPickingTaskCount, "当天"));
        todoTaskList.add(
                new TodoTask("待打包任务", "download-count|svg", todoPackingTaskCount, completedPackingTaskCount, "当天"));
        return todoTaskList;
    }

    @Override
    public List<TrendVo> findInOrderTrend(String warehouseId, Integer year) {
        int targetYear = resolveYear(year);
        return buildMonthTrend(bigscreenMapper.countInOrderByMonth(warehouseId, targetYear), targetYear);
    }

    @Override
    public List<TrendVo> findOutOrderTrend(String warehouseId, Integer year) {
        int targetYear = resolveYear(year);
        return buildMonthTrend(bigscreenMapper.countOutOrderByMonth(warehouseId, targetYear), targetYear);
    }

    /**
     * 年份为空时默认当前年份
     */
    private int resolveYear(Integer year) {
        return (year == null || year <= 0) ? LocalDate.now().getYear() : year;
    }

    /**
     * 把数据库按月分组的结果补齐为12个月，按1月→12月排序，无数据的月份填0
     */
    private List<TrendVo> buildMonthTrend(List<TrendVo> dbList, int year) {
        Map<String, Long> countMap = new HashMap<>();
        for (TrendVo vo : dbList) {
            countMap.put(vo.getName(), vo.getValue() == null ? 0L : vo.getValue());
        }
        List<TrendVo> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String key = String.format("%d-%02d", year, m);
            result.add(new TrendVo(m + "月", countMap.getOrDefault(key, 0L)));
        }
        return result;
    }
}