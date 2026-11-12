package com.yanggu.code.generator.common.domain.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页查询参数基类
 * <p>仅承载查询条件与分页参数，不承载分页结果；分页结果容器由 {@link #toPage()} 产出，返回值统一封装为PageVO</p>
 */
@Data
@Schema(description = "分页查询参数基类")
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 2101820826443930585L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", defaultValue = "1")
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", defaultValue = "10")
    private Long pageSize = 10L;

    /**
     * 排序字段列表，默认按updateTime和id降序
     */
    @Schema(description = "排序字段列表，默认按updateTime和id降序")
    private List<OrderItemQuery> orderItemList = OrderItemQuery.descs("updateTime", "id");

    /**
     * 产出mybatis-plus分页载体，泛型由调用点的目标类型推断
     * <p>调用规约：必须先赋值给显式声明类型的局部变量，再传入mapper方法，不做链式内联</p>
     */
    public <T> Page<T> toPage() {
        return Page.of(this.pageNum, this.pageSize);
    }

    /**
     * 产出mybatis-plus分页载体，并指定是否执行count查询
     */
    public <T> Page<T> toPage(boolean searchCount) {
        return Page.of(this.pageNum, this.pageSize, searchCount);
    }

}
