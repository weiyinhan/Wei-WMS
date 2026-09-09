package org.jeecg.modules.wms.analysis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wms.analysis.vo.TodoTask;
import org.jeecg.modules.wms.analysis.vo.TrendVo;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;

import java.util.List;

/**
 * @Description: 大屏mapper
 * @Author: jeecg-boot
 * @Date:   2025-04-16
 * @Version: V1.0
 */
public interface BigscreenMapper extends BaseMapper<WmsTasks> {

        /**
         * 统计任务
         */
        List<WmsTasks> countTaskList(WmsTasks wmsTasks);

        /**
         * 按月统计入库单数量
         */
        List<TrendVo> countInOrderByMonth(@Param("warehouseId") String warehouseId, @Param("year") Integer year);

        /**
         * 按月统计出库单数量
         */
        List<TrendVo> countOutOrderByMonth(@Param("warehouseId") String warehouseId, @Param("year") Integer year);

}