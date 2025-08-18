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
     * 主键ID
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    @NotNull(message = "模板组ID不能为空")
    private Long templateGroupId;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    @NotNull(message = "父级ID不能为空")
    private Long parentId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    //@NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 文件或者目录名称
     */
    @Schema(description = "文件或者目录名称")
    @NotBlank(message = "文件或者目录名称不能为空")
    private String fileName;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述")
    private String templateDesc;

    /**
     * 模板类型（0-目录，1-模板文件，2-二进制文件）
     */
    @Schema(description = "模板类型（0-目录，1-模板文件，2-二进制文件）")
    @NotNull(message = "模板类型不能为空")
    @EnumCode(TemplateTypeEnum.class)
    private Integer templateType;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    private String templateContent;

    /**
     * 二进制原始文件名
     */
    @Schema(description = "二进制原始文件名")
    private String binaryOriginalFileName;

}