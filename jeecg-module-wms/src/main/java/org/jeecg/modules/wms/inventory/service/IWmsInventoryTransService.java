package org.jeecg.modules.wms.inventory.service;

import org.jeecg.modules.wms.inventory.entity.WmsInventoryTrans;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wms.vo.WmsInventoryTransParam;

/**
 * @Description: 库存变更表
 * @Author: jeecg-boot
 * @Date:   2026-07-30
 * @Version: V1.0
 */
public interface IWmsInventoryTransService extends IService<WmsInventoryTrans> {
    void transfer(WmsInventoryTransParam inventoryTransParam);

}
