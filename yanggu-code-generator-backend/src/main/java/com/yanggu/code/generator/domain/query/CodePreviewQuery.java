package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 代码预览查询实体类
 */
@Data
@Schema(description = "代码预览查询实体类")
public class CodePreviewQuery {

    /**
     * 预览产物ID
     */
    @Schema(description = "预览产物ID")
    private Long previewProductId;

    /**
     * 生成产物类型
     */
    @Schema(description = "生成产物类型")
    private Integer generatorProductType;

}
