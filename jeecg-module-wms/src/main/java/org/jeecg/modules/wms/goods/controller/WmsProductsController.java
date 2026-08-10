package org.jeecg.modules.wms.goods.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import com.alibaba.excel.EasyExcel;
import com.xkcoding.http.support.Http;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

@Tag(name="商品信息")
@RestController
@RequestMapping("/goods/wmsProducts")
@Slf4j
public class WmsProductsController extends JeecgController<WmsProducts, IWmsProductsService> {
	@Autowired
	private IWmsProductsService wmsProductsService;

	@Operation(summary="商品信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WmsProducts>> queryPageList(WmsProducts wmsProducts,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
        QueryWrapper<WmsProducts> queryWrapper = QueryGenerator.initQueryWrapper(wmsProducts, req.getParameterMap());
		IPage<WmsProducts> pageList = wmsProductsService.queryPageList(wmsProducts,
            pageNo,
            pageSize,
            req);
		return Result.OK(pageList);
	}

	@AutoLog(value = "商品信息-添加")
	@Operation(summary="商品信息-添加")
    @RequiresPermissions("goods:wms_products:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WmsProducts wmsProducts) {
		wmsProductsService.save(wmsProducts);
		return Result.OK("添加成功！");
	}

	@AutoLog(value = "商品信息-编辑")
	@Operation(summary="商品信息-编辑")
    @RequiresPermissions("goods:wms_products:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WmsProducts wmsProducts) {
		wmsProductsService.updateById(wmsProducts);
		return Result.OK("编辑成功!");
	}

	@AutoLog(value = "商品信息-通过id删除")
	@Operation(summary="商品信息-通过id删除")
    @RequiresPermissions("goods:wms_products:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		wmsProductsService.removeById(id);
		return Result.OK("删除成功!");
	}

	@AutoLog(value = "商品信息-批量删除")
	@Operation(summary="商品信息-批量删除")
    @RequiresPermissions("goods:wms_products:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wmsProductsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	@Operation(summary="商品信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WmsProducts> queryById(@RequestParam(name="id",required=true) String id) {
		WmsProducts wmsProducts = wmsProductsService.getById(id);
		if(wmsProducts==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(wmsProducts);
	}

    @RequiresPermissions("goods:wms_products:exportXls")
    @RequestMapping(value = "/exportXls")
    public void exportXls(HttpServletRequest request, HttpServletResponse response, WmsProducts wmsProducts) throws IOException {
		Result<IPage<WmsProducts>> iPageResult = this.queryPageList(wmsProducts, 1, 100, request);
		List<WmsProducts> records = iPageResult.getResult().getRecords();

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");
		// 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
		String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
		response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), WmsProducts.class).sheet("模板").doWrite(records);
    
    }
/*
导入
 */
    @RequiresPermissions("goods:wms_products:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, WmsProducts.class);
    }

}
