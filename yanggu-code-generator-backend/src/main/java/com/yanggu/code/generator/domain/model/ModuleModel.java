package com.yanggu.code.generator.domain.model;

import lombok.Data;

/**
 * 模块Model实体类
 */
@Data
public class ModuleModel {

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 模块描述
     */
    private String moduleDesc;

    /**
     * 模块路径
     */
    private String modulePath;

}
