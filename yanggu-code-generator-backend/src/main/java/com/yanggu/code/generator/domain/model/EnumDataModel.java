package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class EnumDataModel {

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
     * 后端路径
     */
    private String backendPath;

    /**
     * 枚举名称
     */
    private String enumName;

    /**
     * 枚举名称（大驼峰）
     */
    private String enumNamePascal;

    /**
     * 枚举描述
     */
    private String enumDesc;

    /**
     * 枚举值列表
     */
    private List<EnumItemDataModel> enumItemList;

}
