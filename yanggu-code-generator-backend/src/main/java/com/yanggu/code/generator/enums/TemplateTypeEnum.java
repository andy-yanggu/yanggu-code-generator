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

    DIRECTORY(0, "目录"),

    TEMPLATE_FILE(1, "模板文件"),

    BINARY_FILE(2, "二进制文件");

    ;

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

}
