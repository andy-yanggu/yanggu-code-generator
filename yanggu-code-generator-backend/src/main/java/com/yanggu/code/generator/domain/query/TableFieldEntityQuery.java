package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表字段Entity查询实体类
 */
@Data
@Schema(description = "表字段Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TableFieldEntityQuery extends PageQuery<TableFieldEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表ID
     */
    @Schema(description = "表ID")
    private Long tableId;

}