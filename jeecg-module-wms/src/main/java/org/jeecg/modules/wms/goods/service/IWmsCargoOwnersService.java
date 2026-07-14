package org.jeecg.modules.wms.goods.service;

import org.jeecg.modules.wms.goods.entity.WmsCargoOwners;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 货主表
 * @Author: jeecg-boot
 * @Date:   2026-07-10
 * @Version: V1.0
 */
public interface IWmsCargoOwnersService extends IService<WmsCargoOwners> {
    /**
     * 货主表
     */
    public void add(WmsCargoOwners wmsCargoOwners);
}
