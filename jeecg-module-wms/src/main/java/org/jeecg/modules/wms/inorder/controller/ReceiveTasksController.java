package org.jeecg.modules.wms.inorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.wms.goods.entity.WmsCargoOwners;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsCargoOwnersService;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrdersService;
import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

/**
 * @Description: 收货任务接口类
 * @Author: jeecg-boot
 * @Date: 2025-08-05
 * @Version: V1.0
 */
@Tag(name = "任务表")
@RestController
@RequestMapping("/inorder/receiveTasks")
@Slf4j
public class ReceiveTasksController {
    @Autowired
    private IWmsTasksService wmsTasksService;

    @Autowired
    private IWmsTasksRecordsService wmsTasksRecordsService;

    @Autowired
    private IWmsStockInOrdersService wmsStockInOrdersService;

    @Autowired
    private IWmsCargoOwnersService wmsCargoOwnersService;

    @Autowired
    private IWmsWarehousesService wmsWarehousesService;

    @Autowired
    private IWmsProductsService wmsProductsService;

    /**
     * 待收货任务查询
     *
     * @param wmsTasks
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "待收货任务查询")
    @GetMapping(value = "/list")
    public Result<IPage<WmsTasks>> receivelist(WmsTasks wmsTasks,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        // 当前用户id
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        wmsTasks.setOperator(sysUser.getId());
        IPage<WmsTasks> wmsTasksIPage = wmsTasksService.list(wmsTasks, pageNo, pageSize);
        return Result.OK(wmsTasksIPage);
    }

    @Operation(summary = "收货记录查询")
    @GetMapping(value = "/records")
    public Result<IPage<WmsTasksRecords>> records(WmsTasksRecords wmsTasksRecords,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<WmsTasksRecords> queryWrapper = QueryGenerator.initQueryWrapper(wmsTasksRecords,
                req.getParameterMap());
        Page<WmsTasksRecords> page = new Page<WmsTasksRecords>(pageNo, pageSize);
        IPage<WmsTasksRecords> pageList = wmsTasksRecordsService.page(page, queryWrapper);

        for (WmsTasksRecords record : pageList.getRecords()) {
            // 通过入库单关联入库单号、货主、仓库
            if (record.getStockInOrderId() != null) {
                WmsStockInOrders order = wmsStockInOrdersService.getById(record.getStockInOrderId());
                if (order != null) {
                    record.setStockInOrderNumber(order.getOrderNumber());
                    if (order.getOwnerId() != null) {
                        WmsCargoOwners owner = wmsCargoOwnersService.getById(order.getOwnerId());
                        if (owner != null) {
                            record.setOwnerName(owner.getOwnerName());
                        }
                    }
                    if (order.getWarehouseId() != null) {
                        WmsWarehouses warehouse = wmsWarehousesService.getById(order.getWarehouseId());
                        if (warehouse != null) {
                            record.setWarehouseName(warehouse.getWarehouseName());
                        }
                    }
                }
            }
            // 商品名称
            if (record.getProductId() != null) {
                WmsProducts product = wmsProductsService.getById(record.getProductId());
                if (product != null) {
                    record.setProductName(product.getProductName());
                }
            }
        }

        return Result.OK(pageList);
    }

    /**
     * 创建收货任务
     * 参数： 入库单id,执行人
     */
    @AutoLog(value = "创建收货任务")
    @Operation(summary = "创建收货任务")
    @RequiresPermissions("inorder:receive_task:add")
    @PostMapping(value = "/add")
    public Result<String> add(String orderIds, String operator) {
        if (StringUtils.isEmpty(orderIds) || StringUtils.isEmpty(operator)) {
            return Result.error("入库单及选择执行人");
        }
        String[] orderIdArray = orderIds.split(",");
        for (String orderId : orderIdArray) {
            wmsTasksService.createReceiveTask(orderId, operator);
        }
        return Result.OK("创建成功！");
    }

    /**
     * 收货
     *
     * @param wmsTasksRecords
     * @return
     */
    @AutoLog(value = "收货")
    @Operation(summary = "收货")
    @RequiresPermissions("inorder:receive_task:addRecords")
    @PostMapping(value = "/addRecords")
    public Result<String> addRecords(@RequestBody WmsTasksRecords wmsTasksRecords) {
        // 任务id
        String taskId = wmsTasksRecords.getId();
        wmsTasksRecords.setId(null);
        wmsTasksRecords.setTaskId(taskId);
        // 执行收货
        wmsTasksService.receive(wmsTasksRecords);
        return Result.OK("添加成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    // @AutoLog(value = "任务表-通过id查询")
    @Operation(summary = "任务表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<WmsTasks> queryById(@RequestParam(name = "id", required = true) String id) {
        WmsTasks wmsTasks = wmsTasksService.getById(id);
        if (wmsTasks == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(wmsTasks);
    }

}