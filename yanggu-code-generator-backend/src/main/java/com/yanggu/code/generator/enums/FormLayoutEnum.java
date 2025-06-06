package com.yanggu.code.generator.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表单布局枚举
 */
@Getter
@AllArgsConstructor
public enum FormLayoutEnum {

    ONE(1, "一列"),

    TWO(2, "两列");

    @JsonValue
    @EnumValue
    private final Integer code;

    private final String description;

}
