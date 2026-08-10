package org.jeecg.modules.wms.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wms.goods.entity.WmsCargoOwners;
import org.jeecg.modules.wms.goods.entity.WmsProductBrand;
import org.jeecg.modules.wms.goods.entity.WmsProductCategories;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.excel.WmsProductsImport;
import org.jeecg.modules.wms.goods.mapper.WmsProductsMapper;
import org.jeecg.modules.wms.goods.service.IWmsCargoOwnersService;
import org.jeecg.modules.wms.goods.service.IWmsProductBrandService;
import org.jeecg.modules.wms.goods.service.IWmsProductCategoriesService;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WmsProductsServiceImpl extends ServiceImpl<WmsProductsMapper, WmsProducts> implements IWmsProductsService {

    @Autowired
    private IWmsCargoOwnersService wmsCargoOwnersService;

    @Autowired
    private IWmsProductBrandService wmsProductBrandService;

    @Autowired
    private IWmsProductCategoriesService wmsProductCategoriesService;

    @Override
    public void importProduct(List<WmsProductsImport> wmsProductsImport) {
    }

    @Override
    public IPage<WmsProducts> queryPageList(WmsProducts wmsProducts,
            Integer pageNo,
            Integer pageSize,
            HttpServletRequest req) {
        // 构建查询条件（支持前端高级查询）
        QueryWrapper<WmsProducts> queryWrapper = QueryGenerator.initQueryWrapper(wmsProducts, req.getParameterMap());

        // 执行分页查询
        IPage<WmsProducts> page = new Page<>(pageNo, pageSize);
        IPage<WmsProducts> pageList = this.page(page, queryWrapper);

        // 获取当前页的数据
        List<WmsProducts> records = pageList.getRecords();

        // 循环设置货主名、品牌名、分类名
        for (WmsProducts record : records) {
            // 设置货主名
            if (StrUtil.isNotEmpty(record.getOwnerId())) {
                WmsCargoOwners owner = wmsCargoOwnersService.getById(record.getOwnerId());
                if (owner != null) {
                    record.setOwnerName(owner.getOwnerName());
                }
            }

            // 设置商品品牌名
            if (StrUtil.isNotEmpty(record.getProductBrand())) {
                WmsProductBrand productBrand = wmsProductBrandService.getById(record.getProductBrand());
                if (productBrand != null) {
                    record.setProductBrandName(productBrand.getName());
                }
            }

            // 设置商品分类名
            if (StrUtil.isNotEmpty(record.getCategoryId())) {
                WmsProductCategories category = wmsProductCategoriesService.getById(record.getCategoryId());
                if (category != null) {
                    record.setCategoryName(category.getCategoryName());
                }
            }
        }

        pageList.setRecords(records);
        return pageList;
    }
}
