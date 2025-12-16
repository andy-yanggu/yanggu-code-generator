package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.util.List;

/**
 * 基类模型实体类
 */
@Data
public class BaseClassModel {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 基类包名
     */
    private String packageName;

    /**
     * 基类类名
     */
    private String className;

    /**
     * 基类字段
     */
    private List<String> fieldList;

    /**
     * 备注
     */
    private String remark;

}