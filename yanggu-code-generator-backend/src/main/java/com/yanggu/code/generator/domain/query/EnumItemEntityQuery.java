package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.OrderItemQuery;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 枚举项Entity查询实体类
 */
@Data
@Schema(description = "枚举项Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class EnumItemEntityQuery extends PageQuery<EnumItemEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 枚举ID
     */
    @Schema(description = "枚举ID")
    private Long enumId;

    /**
     * 枚举项名称
     */
    @Schema(description = "枚举项名称")
    private String enumItemName;

    /**
     * 枚举项编码
     */
    @Schema(description = "枚举项编码")
    private String enumItemCode;

    /**
     * 排序字段列表，默认按enumItemOrder升序和updateTime降序和id降序
     */
    @Schema(description = "排序字段列表，默认按enumItemOrder升序和updateTime降序和id降序")
    private List<OrderItemQuery> orderItemList = List.of(
            OrderItemQuery.asc("enumItemOrder"),
            OrderItemQuery.desc("updateTime"),
            OrderItemQuery.desc("id")
    );

}