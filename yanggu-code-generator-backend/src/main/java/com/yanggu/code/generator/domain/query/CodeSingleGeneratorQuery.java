package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 单个代码生成实体类
 */
@Data
@Schema(description = "单个代码生成实体类")
public class CodeSingleGeneratorQuery {

    /**
     * 项目、表和枚举ID
     */
    @Schema(description = "项目、表和枚举ID")
    private Long id;

    /**
     * 模板组类型
     */
    @Schema(description = "模板组类型")
    private Integer templateGroupType;

    /**
     * 模板ID
     */
    private Long templateId;

}
