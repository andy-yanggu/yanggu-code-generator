package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板拖拽DTO实体类
 */
@Data
@Schema(description = "模板拖拽DTO实体类")
public class TemplateDragDTO {

    /**
     * 拖拽节点ID
     */
    @Schema(description = "拖拽节点ID")
    private Long dragNodeId;

    /**
     * 新父级ID
     */
    @Schema(description = "新父级ID")
    private Long newParentId;

}
