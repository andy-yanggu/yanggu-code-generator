package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基类DTO实体类
 */
@Data
@Schema(description = "基类DTO实体类")
public class BaseClassDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 基类包名
     */
    @Schema(description = "基类包名")
    @NotBlank(message = "基类包名不能为空")
    private String packageName;

    /**
     * 基类编码
     */
    @Schema(description = "基类编码")
    @NotBlank(message = "基类编码不能为空")
    private String code;

    /**
     * 基类字段，多个用英文逗号分隔
     */
    @Schema(description = "基类字段，多个用英文逗号分隔")
    @NotBlank(message = "基类字段不能为空")
    private String fields;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}