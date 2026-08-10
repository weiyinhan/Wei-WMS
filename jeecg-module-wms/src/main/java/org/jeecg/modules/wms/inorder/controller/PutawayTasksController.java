package org.jeecg.modules.wms.inorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wms.goods.entity.WmsCargoOwners;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsCargoOwnersService;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 上架任务接口类
 * @Author: jeecg-boot
 * @Date: 2025-08-05
 * @Version: V1.0
 */
@Tag(name = "上架任务")
@RestController
@RequestMapping("/inorder/putawayTasks")
@Slf4j
public class PutawayTasksController {

    @Autowired
    private IWmsTasksRecordsService wmsTasksRecordsService;

    @Autowired
    private IWmsTasksService wmsTasksService;

    @Autowired
    private IWmsWarehousesService wmsWarehousesService;

    @Autowired
    private IWmsProductsService wmsProductsService;

    @Autowired
    private IWmsCargoOwnersService wmsCargoOwnersService;

    /**
     * 上架记录查询
     *
     * @param wmsTasksRecords
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "上架记录查询")
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
            // 仓库名称：优先取目的仓库，没有则取来源仓库
            String warehouseId = record.getTargetWarehouseId();
            if (warehouseId == null) {
                warehouseId = record.getSourceWarehouseId();
            }
            if (warehouseId != null) {
                WmsWarehouses warehouse = wmsWarehousesService.getById(warehouseId);
                if (warehouse != null) {
                    record.setWarehouseName(warehouse.getWarehouseName());
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
     * 待上架任务查询
     *
     * @param wmsTasks
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "待上架任务查询")
    @GetMapping(value = "/list")
    public Result<IPage<WmsTasks>> list(WmsTasks wmsTasks,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<WmsTasks> queryWrapper = QueryGenerator.initQueryWrapper(wmsTasks, req.getParameterMap());
        Page<WmsTasks> page = new Page<WmsTasks>(pageNo, pageSize);
        IPage<WmsTasks> pageList = wmsTasksService.page(page, queryWrapper);

        for (WmsTasks task : pageList.getRecords()) {
            // 仓库名称
            if (task.getTargetWarehouseId() != null) {
                WmsWarehouses warehouse = wmsWarehousesService.getById(task.getTargetWarehouseId());
                if (warehouse != null) {
                    task.setWarehouseName(warehouse.getWarehouseName());
                }
            }
            // 商品名称
            if (task.getProductId() != null) {
                WmsProducts product = wmsProductsService.getById(task.getProductId());
                if (product != null) {
                    task.setProductName(product.getProductName());
                }
            }
            // 货主名称：通过商品关联货主
            if (task.getProductId() != null) {
                WmsProducts product = wmsProductsService.getById(task.getProductId());
                if (product != null && product.getOwnerId() != null) {
                    WmsCargoOwners owner = wmsCargoOwnersService.getById(product.getOwnerId());
                    if (owner != null) {
                        task.setOwnerName(owner.getOwnerName());
                    }
                }
            }
        }

        return Result.OK(pageList);
    }

    /**
     * 上架
     *
     * @param wmsTasksRecords
     * @return
     */
    @Operation(summary = "上架")
    @PostMapping(value = "/addRecords")
    public Result<String> addRecords(@RequestBody WmsTasksRecords wmsTasksRecords) {
        String taskId = wmsTasksRecords.getId();
        wmsTasksRecords.setId(null);
        wmsTasksRecords.setTaskId(taskId);
        wmsTasksService.putaway(wmsTasksRecords);
        return Result.OK("上架成功");
    }
}
