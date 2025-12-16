package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 基类DTO实体类
 */
@Data
@Schema(description = "基类DTO实体类")
public class BaseClassDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 基类名称
     */
    @Schema(description = "基类名称")
    @NotBlank(message = "基类名称不能为空")
    private String baseClassName;

    /**
     * 基类包名
     */
    @Schema(description = "基类包名")
    @NotBlank(message = "基类包名不能为空")
    private String packageName;

    /**
     * 基类类名
     */
    @Schema(description = "基类类名")
    @NotBlank(message = "基类类名不能为空")
    private String className;

    /**
     * 基类字段
     */
    @Schema(description = "基类字段")
    @NotEmpty(message = "基类字段不能为空")
    private List<String> fieldList;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}