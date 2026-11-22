package com.yanggu.code.generator.domain.model;

import lombok.Data;

/**
 * 枚举项模型实体类
 */
@Data
public class EnumItemModel {

    /**
     * 枚举项名称
     */
    @VarDesc("枚举项名称")
    private String enumItemName;

    /**
     * 枚举项名称（全部大写）
     */
    @VarDesc("枚举项名称（全部大写）")
    private String enumItemNameAllUpper;

    /**
     * 枚举项编码，实际类型由 enumCodeType 指示（String 或 Integer）
     */
    @VarDesc("枚举项编码，实际类型由 enumCodeType 指示（String 或 Integer）")
    private Object enumItemCode;

    /**
     * 枚举项描述
     */
    @VarDesc("枚举项描述")
    private String enumItemDesc;

}