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

    /**
     * 按需装配工厂：调用处显式声明要不要模板列表/属性列表，省去逐个 set 的样板
     */
    public static TemplateGroupDetailQuery of(Long id, Boolean isIncludeTemplateList, Boolean isIncludePropertyList) {
        TemplateGroupDetailQuery query = new TemplateGroupDetailQuery();
        query.setId(id);
        query.setIsIncludeTemplateList(isIncludeTemplateList);
        query.setIsIncludePropertyList(isIncludePropertyList);
        return query;
    }

}
