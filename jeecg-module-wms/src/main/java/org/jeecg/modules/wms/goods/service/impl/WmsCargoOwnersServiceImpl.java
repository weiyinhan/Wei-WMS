package org.jeecg.modules.wms.goods.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.goods.entity.WmsCargoOwners;
import org.jeecg.modules.wms.goods.mapper.WmsCargoOwnersMapper;
import org.jeecg.modules.wms.goods.service.IWmsCargoOwnersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 货主表
 * @Author: jeecg-boot
 * @Date: 2026-07-10
 * @Version: V1.0
 */
@Service
public class WmsCargoOwnersServiceImpl extends ServiceImpl<WmsCargoOwnersMapper, WmsCargoOwners>
        implements IWmsCargoOwnersService {

    private static final String REDIS_KEY = "WMS:CARGO:OWNERS:CODE";
    private static final String CODE_PREFIX = "C";

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void add(WmsCargoOwners wmsCargoOwners) {
        String code = generateOwenerCode();
        wmsCargoOwners.setOwnerCode(code);
        save(wmsCargoOwners);
    }

    public String generateOwenerCode() {
        try {
            if (!redisUtil.hasKey(REDIS_KEY)) {
                initCodeCounter();
            }
            long code = redisUtil.incr(REDIS_KEY, 1);
            return CODE_PREFIX + String.format("%05d", code);
        } catch (Exception e) {
            throw new JeecgBootException("货主编码生成失败");
        }
    }

    private synchronized void initCodeCounter() {
        if (redisUtil.hasKey(REDIS_KEY)) {
            return;
        }
        QueryWrapper<WmsCargoOwners> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("owner_code");
        queryWrapper.orderByDesc("owner_code");
        queryWrapper.last("LIMIT 1");
        WmsCargoOwners lastOne = this.getOne(queryWrapper);
        long maxCode = 0;
        if (lastOne != null && lastOne.getOwnerCode() != null) {
            String numStr = lastOne.getOwnerCode().substring(CODE_PREFIX.length());
            try {
                maxCode = Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                maxCode = 0;
            }
        }
        redisUtil.set(REDIS_KEY, maxCode);
    }
}
