package com.yanggu.code.generator.domain.query;

import lombok.Data;

@Data
public class TemplateGroupDetailQuery {

    private Long id;

    private Boolean isIncludeTemplateList;

    private Boolean isIncludePropertyList;

}
