package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.code.EnumCode;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.common.validation.path.UnixPath;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模板DTO实体类
 */
@Data
@Schema(description = "模板DTO实体类")
public class TemplateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    @NotNull(message = "模板组ID不能为空")
    private Long templateGroupId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 生成代码的路径
     */
    @Schema(description = "生成代码的路径")
    @NotBlank(message = "生成代码的路径不能为空")
    @UnixPath(message = "生成代码的路径格式不正确")
    private String generatorPath;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述")
    private String templateDesc;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    private String templateContent;

    /**
     * 模板类型（0-文件，1-目录）
     */
    @Schema(description = "模板类型（0-文件，1-目录）")
    @NotNull(message = "模板类型不能为空")
    @EnumCode(TemplateTypeEnum.class)
    private Integer templateType;

}