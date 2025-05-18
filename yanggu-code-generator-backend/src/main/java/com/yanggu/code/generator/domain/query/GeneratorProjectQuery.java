package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 生成项目查询实体类
 */
@Data
@Schema(description = "生成项目查询实体类")
public class GeneratorProjectQuery {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 表ID列表
     */
    @Schema(description = "表ID列表")
    private List<Long> tableIdList;

    /**
     * 项目模板ID列表
     */
    @Schema(description = "项目模板ID列表")
    private List<Long> projectTemplateIdList;

    /**
     * 表模板ID列表
     */
    @Schema(description = "表模板ID列表")
    private List<Long> tableTemplateIdList;

}
