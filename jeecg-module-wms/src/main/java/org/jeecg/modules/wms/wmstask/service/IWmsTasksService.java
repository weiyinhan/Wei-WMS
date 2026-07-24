package org.jeecg.modules.wms.wmstask.service;

import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 任务表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
public interface IWmsTasksService extends IService<WmsTasks> {
    public void createReceiveTask(String orderId,String  operator);
}
