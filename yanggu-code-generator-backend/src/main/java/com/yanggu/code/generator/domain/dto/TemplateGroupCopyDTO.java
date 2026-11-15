package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 模板组复制DTO实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模板组复制DTO实体类")
public class TemplateGroupCopyDTO extends TemplateGroupDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 被复制的原模板组ID
     */
    @Schema(description = "被复制的原模板组ID")
    @NotNull(message = "原模板组ID不能为空")
    private Long oldGroupId;

}
