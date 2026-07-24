package org.jeecg.modules.wms.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.excel.WmsProductsImport;

import java.util.List;

public interface IWmsProductsService extends IService<WmsProducts> {
    public void importProduct(List<WmsProductsImport> wmsProductsImport);
}
