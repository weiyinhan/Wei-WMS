package org.jeecg.modules.wms.waybill.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.jeecg.modules.wms.config.ThreadPoolConfiguration;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersService;
import org.jeecg.modules.wms.waybill.task.handler.SimpleThreadCallable;
import org.jeecg.modules.wms.waybill.task.handler.WaybillThreadCallable;
import org.jeecg.modules.wms.waybill.service.IWmsWaybillService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 生成运单定时任务
 * @date 2025/7/15 11:51
 */
@Component
@Slf4j
public class WaybillTaskJob {
    @Autowired
    private IWmsWaybillService wmsWaybillService;

    @Autowired
    private IWmsOutOrdersService wmsOutOrdersService;

    @Resource(name = "waybillThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    //任务是否正在运行
    private volatile boolean isRunning = false;
//    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void test1() {
        log.info("============--开始执行定时任务--------------");
        //向线程池放入十个任务
        for(int i=0;i<10;i++){
            SimpleThreadCallable simpleThreadCallable = new SimpleThreadCallable();
            threadPoolExecutor.submit(simpleThreadCallable);
        }
    }
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void test2() {
        //防止重入
        if(isRunning){
            log.info("上一轮任务还在运行");
            return;
        }
        //如果上一轮任务没有完成不再继续执行
        log.info("============--开始执行定时任务--------------");
        List<Future<Boolean>> futureList= new ArrayList<>();
        //定义List<Boolean>存入任务对象
        //向线程池放入十个任务
        for(int i=0;i<10;i++){
            SimpleThreadCallable simpleThreadCallable = new SimpleThreadCallable();
            Future future = threadPoolExecutor.submit(simpleThreadCallable);
            futureList.add(future);
        }
        //判断任务是否已近全部完成
        for(Future<Boolean> future:futureList){
            try {
                //阻塞方法
                Boolean result = future.get(30,TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("任务执行异常");
                future.cancel(true);
            }
        }
        //执行完成
        isRunning =false;
    }
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void test3() {
        //防止重入
        if(isRunning){
            log.info("上一轮任务还在运行");
            return;
        }
        //如果上一轮任务没有完成不再继续执行
        log.info("============--开始执行定时任务--------------");
        isRunning =true;
        List<Future<Boolean>> futureList= new ArrayList<>();
        //查询待生成运单的出库单（具体条件根据业务调整）
        List<WmsOutOrders> records = wmsOutOrdersService.list();
        //向线程池放入任务
        for(WmsOutOrders orders:records){
            //创建生成运单的任务，传入出库单id
            WaybillThreadCallable waybillTaskCallable = new WaybillThreadCallable(wmsWaybillService, orders.getId());
            Future future = threadPoolExecutor.submit(waybillTaskCallable);
            futureList.add(future);
        }
        //判断任务是否已近全部完成
        for(Future<Boolean> future:futureList){
            try {
                //阻塞方法
                Boolean result = future.get(30,TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("任务执行异常");
                future.cancel(true);
            }
        }
        //执行完成
        isRunning =false;
    }

}





















