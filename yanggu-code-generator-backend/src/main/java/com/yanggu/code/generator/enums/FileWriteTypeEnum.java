package com.yanggu.code.generator.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 文件写入方式枚举
 */
@Getter
@AllArgsConstructor
public enum FileWriteTypeEnum {

    /**
     * 覆盖
     */
    OVERRIDE(0, "覆盖"),

    /**
     * 追加
     */
    APPEND(1, "追加"),
    ;

    /**
     * code值
     */
    @JsonValue
    @EnumValue
    private final Integer code;

    /**
     * 描述
     */
    private final String description;

}
