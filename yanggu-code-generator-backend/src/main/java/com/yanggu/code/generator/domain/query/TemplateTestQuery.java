package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板测试实体类
 */
@Data
@Schema(description = "模板测试实体类")
public class TemplateTestQuery {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 测试ID
     */
    @Schema(description = "测试ID")
    private Long testId;

    /**
     * 模板组类型
     */
    @Schema(description = "模板组类型")
    private Integer templateGroupType;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    private Long templateGroupId;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private Long templateId;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    private String templateContent;

}
