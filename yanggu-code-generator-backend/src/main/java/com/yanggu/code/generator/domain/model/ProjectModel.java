package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.util.List;

/**
 * 项目模型实体类
 */
@Data
public class ProjectModel {

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
     * 项目名称（大写空格）（Test1 Demo）
     */
    private String projectNameUpperSpace;

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
     * 项目版本
     */
    private String projectVersion;

    /**
     * 后端路径
     */
    private String backendPath;

    /**
     * 前端路径
     */
    private String frontendPath;

    /**
     * 项目描述
     */
    private String projectDesc;

    /**
     * 数据库驱动类名
     */
    private String databaseDriverClassName;

    /**
     * 数据库连接
     */
    private String databaseUrl;

    /**
     * 数据库用户名
     */
    private String databaseUsername;

    /**
     * 数据库密码
     */
    private String databasePassword;

    /**
     * 表模型数据列表
     */
    private List<TableModel> tableModelList;

    /**
     * 枚举模型数据列表
     */
    private List<EnumModel> enumModelList;

}
