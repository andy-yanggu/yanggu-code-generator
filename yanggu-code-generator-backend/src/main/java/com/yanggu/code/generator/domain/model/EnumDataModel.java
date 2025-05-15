package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class EnumDataModel {

    /**
     * 表名（下划线）
     */
    private String tableName;

    /**
     * 字段名（下划线）
     */
    private String fieldName;

    /**
     * 字段注释
     */
    private String fieldComment;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 枚举值列表
     */
    private List<EnumValueModel> valueList;

    @Data
    public static class EnumValueModel {

        /**
         * 展示值
         */
        private String label;

        /**
         * 枚举值
         */
        private String value;

    }

}
