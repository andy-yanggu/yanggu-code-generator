package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表查询实体类
 */
@Data
@Schema(description = "表查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TableQuery extends PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 项目ID列表
     */
    @Schema(description = "项目ID列表")
    private List<Long> projectIdList;

    /**
     * 数据库名称
     */
    @Schema(description = "数据库名称")
    private String databaseName;

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 类名
     */
    @Schema(description = "类名")
    private String className;

}