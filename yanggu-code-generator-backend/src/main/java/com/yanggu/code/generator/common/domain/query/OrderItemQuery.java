package com.yanggu.code.generator.common.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.hutool.core.text.StrUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排序字段查询参数
 */
@Data
@Schema(description = "排序字段查询参数")
public class OrderItemQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要进行排序的字段
     */
    @Schema(description = "需要进行排序的字段")
    private String column;

    /**
     * 是否正序排列，默认 true
     */
    @Schema(description = "是否正序排列，默认 true")
    private Boolean asc = true;

    public static OrderItemQuery asc(String column) {
        return build(column, true);
    }

    public static OrderItemQuery desc(String column) {
        return build(column, false);
    }

    public static List<OrderItemQuery> ascs(String... columns) {
        return Arrays.stream(columns).map(OrderItemQuery::asc).collect(Collectors.toList());
    }

    public static List<OrderItemQuery> descs(String... columns) {
        return Arrays.stream(columns).map(OrderItemQuery::desc).collect(Collectors.toList());
    }

    private static OrderItemQuery build(String column, boolean asc) {
        return new OrderItemQuery().setColumn(column).setAsc(asc);
    }

    public OrderItemQuery setColumn(String column) {
        this.column = StrUtil.trim(column);
        return this;
    }

    public OrderItemQuery setAsc(boolean asc) {
        this.asc = asc;
        return this;
    }

}
