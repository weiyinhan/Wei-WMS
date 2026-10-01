package org.jeecg.modules.wms.analysis.service;

import org.jeecg.modules.wms.analysis.vo.TodoTask;
import org.jeecg.modules.wms.analysis.vo.TrendVo;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 首页大屏接口
 * @date 2025/8/13 19:02
 */
public interface BigscreenService {

    /**
     * 查询待办任务
     */
    public List<TodoTask> findTodoTaskList(String warehouseId);

    /**
     * 查询入库趋势（按月统计，按1月→12月排序，无数据的月份补0）
     */
    List<TrendVo> findInOrderTrend(String warehouseId, Integer year);

    /**
     * 查询出库趋势（按月统计，按1月→12月排序，无数据的月份补0）
     */
    List<TrendVo> findOutOrderTrend(String warehouseId, Integer year);
}