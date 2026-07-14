package org.jeecg.modules.wms.warehouse.service;

import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 仓库表
 * @Author: jeecg-boot
 * @Date:   2026-07-11
 * @Version: V1.0
 */
public interface IWmsWarehousesService extends IService<WmsWarehouses> {
    public void add(WmsWarehouses wmsWarehouses);
    public void edit(WmsWarehouses wmsWarehouses);
    public void enable(String id);
    public void disable(String id);

}
