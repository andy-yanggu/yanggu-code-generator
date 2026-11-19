package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "模板组详情查询参数")
public class TemplateGroupDetailQuery {

    @Schema(description = "模板组ID")
    private Long id;

    @Schema(description = "是否包含模板列表")
    private Boolean isIncludeTemplateList;

    @Schema(description = "是否包含属性列表")
    private Boolean isIncludePropertyList;

}
