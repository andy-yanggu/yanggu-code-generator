package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 项目生成的子产物过滤（仅PROJECT）
 */
@Data
@Schema(description = "项目生成的子产物过滤（仅PROJECT）")
public class ProjectFilter {

    @Schema(description = "项目内表ID过滤，空=项目下全部表")
    private List<Long> tableIds;

    @Schema(description = "项目内枚举ID过滤，空=全部枚举")
    private List<Long> enumIds;

    @Schema(description = "表模板ID过滤，空=表模板组全量")
    private List<Long> tableTemplateIds;

    @Schema(description = "枚举模板ID过滤，空=枚举模板组全量")
    private List<Long> enumTemplateIds;
}