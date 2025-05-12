package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目下的表查询实体类
 */
@Data
@Schema(description = "项目下的表查询实体类")
public class ProjectTableQuery {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 表名
     */
    private String tableName;

}
