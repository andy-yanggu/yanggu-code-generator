package com.yanggu.code.generator.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 树类型枚举
 */
@Getter
@AllArgsConstructor
public enum TreeTypeEnum {

    FILE(0, "文件"),

    DIRECTORY(1, "目录"),
    ;

    private final Integer code;

    private final String description;

}
