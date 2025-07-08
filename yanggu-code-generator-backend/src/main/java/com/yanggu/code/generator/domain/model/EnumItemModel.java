package com.yanggu.code.generator.domain.model;

import lombok.Data;

@Data
public class EnumItemModel {

    /**
     * 枚举项名称
     */
    private String enumItemName;

    /**
     * 枚举项名称（全部大写）
     */
    private String enumItemNameAllUpper;

    /**
     * 枚举项编码
     */
    private String enumItemCode;

    /**
     * 枚举项描述
     */
    private String enumItemDesc;

}