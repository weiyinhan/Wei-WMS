package org.jeecg.modules.wms.shipment.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wms.shipment.entity.WmsShipmentDetail;
import org.jeecg.modules.wms.shipment.entity.WmsShipment;
import org.jeecg.modules.wms.shipment.vo.WmsShipmentPage;
import org.jeecg.modules.wms.shipment.service.IWmsShipmentService;
import org.jeecg.modules.wms.shipment.service.IWmsShipmentDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;


@Tag(name = "包裹主表")
@RestController
@RequestMapping("/shipment/wmsShipment")
@Slf4j
public class WmsShipmentController {

    /**
     * 完成某波次的打包
     */
    @Autowired
    IWmsShipmentService wmsShipmentService;

    @AutoLog(value = "包裹主表-完成某波次的打包")
    @Operation(summary = "包裹主表-完成某波次的打包")
    @PostMapping(value = "/completePackByWave")
    public Result<String> completePackByWave(@RequestParam(name = "ids", required = true) String ids) {
        if (StringUtils.isEmpty(ids)) {
            throw new JeecgBootException("请选择波次");
        }
        String[] waveIdsArray = ids.split(",");
        for (String waveId : waveIdsArray) {
            wmsShipmentService.completePackByWave(waveId);
        }
        return Result.OK("完成打包成功");
    }
}
