package com.yanggu.code.generator.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 生成产物类型枚举
 */
@Getter
@AllArgsConstructor
public enum GeneratorProductTypeEnum {

    PROJECT(0, "项目"),
    TABLE(1, "表"),
    ENUM(2, "枚举");

    private final Integer productType;

    private final String description;

}
