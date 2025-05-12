package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.TableEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 表Entity查询实体类
 */
@Data
@Schema(description = "表Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TableEntityQuery extends PageQuery<TableEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

}