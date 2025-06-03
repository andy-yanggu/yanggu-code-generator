package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板内容VO实体类
 */
@Data
@Schema(description = "模板内容VO实体类")
public class TemplateContentVO {

    /**
     * 表ID
     */
    @Schema(description = "表ID")
    private Long tableId;

    /**
     * 枚举ID
     */
    @Schema(description = "枚举ID")
    private Long enumId;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private Long templateId;

    /**
     * 模板类型（0-文件，1-目录）
     */
    @Schema(description = "模板类型（0-文件，1-目录）")
    private Integer templateType;

    /**
     * 模板组类型（0-项目模板，1-表模板）
     */
    @Schema(description = "模板组类型（0-项目模板，1-表模板）")
    private Integer templateGroupType;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件内容
     */
    @Schema(description = "文件内容")
    private String content;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String filePath;

}