package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 表数据模型
 */
@Data
public class TableDataModel {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名（test1-demo）
     */
    private String projectName;

    /**
     * 项目名（下划线）（test1_demo）
     */
    private String projectNameUnderline;

    /**
     * 项目名（大驼峰）（Test1Demo）
     */
    private String projectNamePascal;

    /**
     * 项目名（点）（test1.demo）
     */
    private String projectNameDot;

    /**
     * 项目名（斜杠）（test1/demo）
     */
    private String projectNameSlash;

    /**
     * 项目包名（com.yanggu）
     */
    private String projectPackage;

    /**
     * 项目包名（斜杠）（com/yanggu）
     */
    private String projectPackageSlash;

    /**
     * 版本
     */
    private String version;

    /**
     * 功能名（小驼峰）例如表名为template_group，则功能名为templateGroup
     */
    private String functionName;

    /**
     * 功能名（大驼峰）例如表名为template_group，则功能名为TemplateGroup
     */
    private String functionNamePascal;

    /**
     * 功能名（中横线）例如表名为template_group，则功能名为template-group
     */
    private String functionNameKebabCase;

    private int formLayout;

    /**
     * 作者
     */
    private String author;

    /**
     * 日期时间：yyyy-MM-dd HH:mm:ss
     */
    private String datetime;

    /**
     * 日期：yyyy-MM-dd
     */
    private String date;

    /**
     * Entity继承基类
     */
    private BaseClassModel baseClass;

    private Set<String> importList;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 表注释
     */
    private String tableComment;

    private String className;

    private String classNameUpper;

    private List<TableFieldModel> fieldList;

    /**
     * 后端路径
     */
    private String backendPath;

    /**
     * 前端路径
     */
    private String frontendPath;

    /**
     * 生成方式  0：zip压缩包   1：服务器本地
     */
    private Integer generatorType;

    private String dbType;

    /**
     * 主键列表 (支持多主键)
     */
    private List<TableFieldModel> primaryList;

    // 表单列表
    private List<TableFieldModel> formList;

    // 网格列表
    private List<TableFieldModel> gridList;

    // 查询列表
    private List<TableFieldModel> queryList;

    private String templateName;

}
