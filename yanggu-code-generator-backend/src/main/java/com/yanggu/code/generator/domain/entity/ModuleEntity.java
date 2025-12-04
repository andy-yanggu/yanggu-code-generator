package com.yanggu.code.generator.domain.entity;

import lombok.Data;

/**
 * 模块Entity实体类
 */
@Data
public class ModuleEntity {

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
