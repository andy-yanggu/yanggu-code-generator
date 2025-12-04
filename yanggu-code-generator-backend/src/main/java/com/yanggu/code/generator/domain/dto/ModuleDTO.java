package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模块DTO实体类
 */
@Data
@Schema(description = "模块DTO实体类")
public class ModuleDTO {

    /**
     * 模块名称
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 模块描述
     */
    @Schema(description = "模块描述")
    private String moduleDesc;

    /**
     * 模块路径
     */
    @Schema(description = "模块路径")
    private String modulePath;

}
