package org.jeecg.modules.wms.warehouse.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import org.jeecg.modules.wms.warehouse.mapper.WmsStorageZonesMapper;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageZonesService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 储区表
 * @Author: jeecg-boot
 * @Date:   2026-07-11
 * @Version: V1.0
 */
@Service
public class WmsStorageZonesServiceImpl extends ServiceImpl<WmsStorageZonesMapper, WmsStorageZones> implements IWmsStorageZonesService {

    @Override
    public IPage<WmsStorageZones> queryList(WmsStorageZones wmsStorageZones, Integer pageNo, Integer pageSize) {
        //设置分页参数
        Page<WmsStorageZones> objects = PageHelper.startPage(pageNo, pageSize);
        //当执行mapper时PageHelper会自动拼接分页sql
        List<WmsStorageZones> wmsStorageZones1 = getBaseMapper().queryList(wmsStorageZones);
        PageDTO<WmsStorageZones> objectPageDTO = new PageDTO<>();
        objectPageDTO.setRecords(wmsStorageZones1);
        objectPageDTO.setTotal(objects.getTotal());
        objectPageDTO.setCurrent(pageNo);
        objectPageDTO.setSize(pageSize);


        return objectPageDTO;
    }
}
