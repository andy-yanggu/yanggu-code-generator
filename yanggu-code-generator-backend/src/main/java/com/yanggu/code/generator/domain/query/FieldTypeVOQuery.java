package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.FieldTypeVO;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 字段类型VO查询实体类
 */
@Data
@Schema(description = "字段类型VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class FieldTypeVOQuery extends PageQuery<FieldTypeVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字段类型
     */
    @Schema(description = "字段类型")
    private String columnType;

    /**
     * 属性类型
     */
    @Schema(description = "属性类型")
    private String attrType;

}