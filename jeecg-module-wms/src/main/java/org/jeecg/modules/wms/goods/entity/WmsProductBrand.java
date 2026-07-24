package org.jeecg.modules.wms.goods.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("wms_product_brand")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "商品品牌")
public class WmsProductBrand implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    @Schema(description = "创建人")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private Date updateTime;

    @Schema(description = "所属部门")
    private String sysOrgCode;

    @TableLogic
    @Schema(description = "删除状态")
    private Integer delFlag;

    @Excel(name = "品牌名称", width = 15)
    @Schema(description = "品牌名称")
    private String name;

    @Excel(name = "品牌logo", width = 15)
    @Schema(description = "品牌logo")
    private String logo;

    @Excel(name = "状态", width = 15, dicCode = "dict_item_status")
    @Dict(dicCode = "dict_item_status")
    @Schema(description = "状态")
    private String status;

    @Schema(description = "租户ID")
    private Integer tenantId;
}
