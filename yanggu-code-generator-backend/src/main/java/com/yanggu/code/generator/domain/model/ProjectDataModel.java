package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.util.List;

/**
 * 项目数据模型
 */
@Data
public class ProjectDataModel {

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
    private List<TableDataModel> tableDataModelList;

    /**
     * 枚举模型数据列表
     */
    private List<EnumDataModel> enumDataModelList;

}
