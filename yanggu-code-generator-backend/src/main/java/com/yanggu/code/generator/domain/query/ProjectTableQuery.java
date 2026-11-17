package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
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
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 表名
     */
    @Schema(description = "表名")
    @Pattern(regexp = "^[A-Za-z0-9_$]*$", message = "表名仅支持字母、数字、下划线、$")
    private String tableName;

}
