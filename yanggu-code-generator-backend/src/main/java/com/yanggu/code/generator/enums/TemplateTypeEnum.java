package com.yanggu.code.generator.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板类型枚举
 */
@Getter
@AllArgsConstructor
public enum TemplateTypeEnum {

    FILE(0, "文件"),

    DIRECTORY(1, "目录"),
    ;

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

}
