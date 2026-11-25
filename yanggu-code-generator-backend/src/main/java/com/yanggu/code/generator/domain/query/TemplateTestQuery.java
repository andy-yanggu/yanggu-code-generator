package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 测试ID（表/枚举测试产物ID，项目类型不需要，条件必填由service按模板组类型兜底）
     */
    @Schema(description = "测试ID")
    private Long testId;

    /**
     * 模板组类型
     */
    @NotNull(message = "模板组类型不能为空")
    @Schema(description = "模板组类型")
    private Integer templateGroupType;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    private Long templateGroupId;

    /**
     * 模板ID（模板测试渲染必填，校验在 service 代码中做）
     */
    @Schema(description = "模板ID")
    private Long templateId;

    /**
     * 模板内容（模板测试渲染必填，校验在 service 代码中做）
     */
    @Schema(description = "模板内容")
    private String templateContent;

}
