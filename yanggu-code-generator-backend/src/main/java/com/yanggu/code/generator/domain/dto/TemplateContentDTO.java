package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模板内容DTO实体类
 */
@Data
@Schema(description = "模板内容DTO实体类")
public class TemplateContentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    private Long id;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    private String templateContent;

}