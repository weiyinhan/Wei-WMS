package org.jeecg.modules.wms.goods.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.excel.WmsProductsImport;
import org.jeecg.modules.wms.goods.mapper.WmsProductsMapper;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WmsProductsServiceImpl extends ServiceImpl<WmsProductsMapper, WmsProducts> implements IWmsProductsService {

    @Override
    public void importProduct(List<WmsProductsImport> wmsProductsImport) {

    }
}
