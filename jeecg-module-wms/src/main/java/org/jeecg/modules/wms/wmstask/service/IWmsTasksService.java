package org.jeecg.modules.wms.wmstask.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;

/**
 * @Description: 任务表
 * @Author: jeecg-boot
 * @Date: 2026-07-23
 * @Version: V1.0
 */
public interface IWmsTasksService extends IService<WmsTasks> {
    public void createReceiveTask(String orderId, String operator);

    /*
     * 创建收获任务
     */
    public IPage<WmsTasks> list(WmsTasks wmsTasks, Integer pageNo, Integer pageSize);

    /**
     * 执行收货
     */
    public void receive(WmsTasksRecords wmsTasksRecords);
    /**
     * 执行任务
     */
    public WmsTasks execute(WmsTasksRecords wmsTasksRecords);

    /**
     * 执行上架
     */
    public void putaway(WmsTasksRecords wmsTasksRecords);
}
