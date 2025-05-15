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
     * 模板名称
     */
    private String templateName;

    /**
     * 数据库驱动类名
     */
    private String databaseDriverClassName;

    /**
     * 数据库连接
     */
    private String dataBaseUrl;

    /**
     * 数据库用户名
     */
    private String dataBaseUsername;

    /**
     * 数据库密码
     */
    private String dataBasePassword;

    /**
     * 枚举数据模型
     */
    private List<EnumDataModel> enumDataModelList;

}
