package com.yanggu.code.generator.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateGroupTypeEnum {

    PROJECT(0, "项目模板"),

    TABLE(1, "表模板"),
    ;

    /**
     * 编码
     */
    @JsonValue
    @EnumValue
    private final Integer code;

    /**
     * 描述
     */
    private final String description;

}
