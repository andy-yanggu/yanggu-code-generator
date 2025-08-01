package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 生成枚举查询实体类
 */
@Data
@Schema(description = "生成枚举查询实体类")
public class GeneratorEnumQuery {

    /**
     * 枚举D列表
     */
    @Schema(description = "枚举ID列表")
    private List<Long> enumIdList;

    /**
     * 模板ID列表
     */
    @Schema(description = "模板ID列表")
    private List<Long> templateIdList;

}
