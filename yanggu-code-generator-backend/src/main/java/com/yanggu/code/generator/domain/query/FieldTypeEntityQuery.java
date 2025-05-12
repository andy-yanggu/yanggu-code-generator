package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.FieldTypeEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 字段类型Entity查询实体类
 */
@Data
@Schema(description = "字段类型Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class FieldTypeEntityQuery extends PageQuery<FieldTypeEntity> implements Serializable {

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