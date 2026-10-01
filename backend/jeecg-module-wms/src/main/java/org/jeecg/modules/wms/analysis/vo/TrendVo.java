        package org.jeecg.modules.wms.analysis.vo;

        import io.swagger.v3.oas.annotations.media.Schema;
        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.NoArgsConstructor;

/**
 * @Description: 图表趋势VO（name=月份，value=数量）
 * @Author: jeecg-boot
 * @Date: 2026-09-08
 * @Version: V1.0
 */
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @Schema(description = "图表趋势")
        public class TrendVo {
        /** 名称 (月)*/
        @Schema(description = "名称")
        private String name;
        /** 数量 */
        @Schema(description = "数量")
        private Long value;
        }