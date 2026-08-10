package org.jeecg.modules.wms.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.excel.WmsProductsImport;

import java.util.List;

public interface IWmsProductsService extends IService<WmsProducts> {
    void importProduct(List<WmsProductsImport> wmsProductsImport);
    /**商品表导入数据
     */
    IPage<WmsProducts> queryPageList(WmsProducts wmsProducts,
                                     Integer pageNo,
                                     Integer pageSize,
                                     HttpServletRequest req);

}
