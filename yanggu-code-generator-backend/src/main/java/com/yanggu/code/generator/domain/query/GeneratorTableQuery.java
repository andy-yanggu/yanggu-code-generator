package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 生成表查询实体类
 */
@Data
@Schema(description = "生成表查询实体类")
public class GeneratorTableQuery {

    /**
     * 表ID列表
     */
    @Schema(description = "表ID列表")
    private List<Long> tableIdList;

    /**
     * 模板ID列表
     */
    @Schema(description = "模板ID列表")
    private List<Long> templateIdList;

}
