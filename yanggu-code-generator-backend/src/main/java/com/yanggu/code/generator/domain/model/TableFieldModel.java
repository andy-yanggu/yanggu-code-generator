package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表字段模型实体类
 */
@Data
public class TableFieldModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 表ID
     */
    private Long tableId;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 字段说明
     */
    private String fieldComment;

    /**
     * 属性名
     */
    private String attrName;

    /**
     * 属性名大驼峰。例如createTime变成CreateTime
     */
    private String attrNamePascal;

    /**
     * 属性类型
     */
    private String attrType;

    /**
     * 属性包名
     */
    private String packageName;

    /**
     * 字段顺序
     */
    private Integer fieldSort;

    /**
     * 自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE
     */
    private String autoFill;

    /**
     * 主键 0：否  1：是
     */
    private Integer primaryPk;

    /**
     * 逻辑删除 0：否  1：是
     */
    private Integer logicDelete;

    /**
     * 逻辑删除值
     */
    private String logicDeleteValue;

    /**
     * 逻辑未删除值
     */
    private String logicNotDeleteValue;

    /**
     * 枚举ID
     */
    private Long enumId;

    /**
     * 枚举名称
     */
    private String enumName;

    /**
     * 枚举名称（大驼峰）
     */
    private String enumNamePascal;

    /**
     * 枚举名称大写
     */
    private String enumNameAllUpper;

    /**
     * Entity基类字段 0：否  1：是
     */
    private Integer entityBaseField;

    /**
     * VO基类字段 0：否  1：是
     */
    private Integer voBaseField;

    /**
     * 表单项 0：否  1：是
     */
    private Integer formItem;

    /**
     * 表单必填 0：否  1：是
     */
    private Integer formRequired;

    /**
     * 表单类型
     */
    private String formType;

    /**
     * 表单效验
     */
    private String formValidator;

    /**
     * 表单字段顺序
     */
    private Integer formFieldSort;

    /**
     * 列表项 0：否  1：是
     */
    private Integer gridItem;

    /**
     * 列表排序 0：否  1：是
     */
    private Integer gridSort;

    /**
     * 列表字段顺序
     */
    private Integer gridFieldSort;

    /**
     * 查询项 0：否  1：是
     */
    private Integer queryItem;

    /**
     * 查询方式
     */
    private String queryType;

    /**
     * 查询表单类型
     */
    private String queryFormType;

    /**
     * 查询字段顺序
     */
    private Integer queryFieldSort;

}