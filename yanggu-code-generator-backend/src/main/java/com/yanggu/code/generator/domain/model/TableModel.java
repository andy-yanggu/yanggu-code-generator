package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 表数据模型
 */
@Data
public class TableModel {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称（test1-demo）
     */
    private String projectName;

    /**
     * 项目名称（下划线）（test1_demo）
     */
    private String projectNameUnderline;

    /**
     * 项目名称（大驼峰）（Test1Demo）
     */
    private String projectNamePascal;

    /**
     * 项目名称（点）（test1.demo）
     */
    private String projectNameDot;

    /**
     * 项目名称（斜杠）（test1/demo）
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
    private BaseClassModel entityBaseClass;

    /**
     * VO继承基类
     */
    private BaseClassModel voBaseClass;

    /**
     * 导入列表
     */
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

    /**
     * 类名（小写）
     */
    private String className;

    /**
     * 类名（大写）
     */
    private String classNameUpper;

    /**
     * 字段列表
     */
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

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * 主键列表 (支持多主键)
     */
    private List<TableFieldModel> primaryList;

    /**
     * 表单列表
     */
    private List<TableFieldModel> formList;

    /**
     * 网格列表
     */
    private List<TableFieldModel> gridList;

    /**
     * 查询列表
     */
    private List<TableFieldModel> queryList;

}
