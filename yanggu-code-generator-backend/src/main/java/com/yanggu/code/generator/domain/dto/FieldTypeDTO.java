package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字段类型DTO实体类
 */
@Data
@Schema(description = "字段类型DTO实体类")
public class FieldTypeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 字段类型
     */
    @Schema(description = "字段类型")
    @NotBlank(message = "字段类型不能为空")
    private String columnType;

    /**
     * 属性类型
     */
    @Schema(description = "属性类型")
    @NotBlank(message = "属性类型不能为空")
    private String attrType;

    /**
     * 属性包名
     */
    @Schema(description = "属性包名")
    private String packageName;

}