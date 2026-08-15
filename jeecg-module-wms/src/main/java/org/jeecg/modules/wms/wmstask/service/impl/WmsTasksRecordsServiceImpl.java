package org.jeecg.modules.wms.wmstask.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.mapper.WmsTasksRecordsMapper;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 任务执行记录表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
@Service
public class WmsTasksRecordsServiceImpl extends ServiceImpl<WmsTasksRecordsMapper, WmsTasksRecords> implements IWmsTasksRecordsService {

    @Override
    public IPage<WmsTasksRecords> pageList(WmsTasksRecords wmsTasksRecords, Integer pageNo, Integer pageSize) {
        Page<WmsTasksRecords> page = PageHelper.startPage(pageNo, pageSize);
        List<WmsTasksRecords> list = baseMapper.queryRecordsList(wmsTasksRecords);
        PageDTO<WmsTasksRecords> pageDTO = new PageDTO<>();
        pageDTO.setRecords(list);
        pageDTO.setTotal(page.getTotal());
        pageDTO.setSize(page.getPageSize());
        pageDTO.setCurrent(page.getPageNum());
        pageDTO.setPages(page.getPages());
        return pageDTO;
    }
}
