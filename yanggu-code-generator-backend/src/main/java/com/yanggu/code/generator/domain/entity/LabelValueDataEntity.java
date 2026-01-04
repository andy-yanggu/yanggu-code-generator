package com.yanggu.code.generator.domain.entity;

import lombok.Data;

@Data
public class LabelValueDataEntity {

    /**
     * 标签
     */
    private String label;

    /**
     * 值
     */
    private Object value;

}
