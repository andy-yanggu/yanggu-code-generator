package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 枚举DTO实体类
 */
@Data
@Schema(description = "枚举DTO实体类")
public class EnumDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 枚举名称
     */
    @Schema(description = "枚举名称")
    @NotBlank(message = "枚举名称不能为空")
    private String enumName;

    /**
     * 枚举描述
     */
    @Schema(description = "枚举描述")
    @NotBlank(message = "枚举描述不能为空")
    private String enumDesc;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 模板组数据
     */
    @Schema(description = "模板组数据")
    private Map<String, Object> templateGroupPropertyData;

}