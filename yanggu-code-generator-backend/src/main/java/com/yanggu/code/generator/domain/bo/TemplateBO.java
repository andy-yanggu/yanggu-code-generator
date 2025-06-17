package com.yanggu.code.generator.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模板BO实体类
 */
@Data
public class TemplateBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 生成代码的路径
     */
    private String generatorPath;

    /**
     * 模板描述
     */
    private String templateDesc;

    /**
     * 模板内容
     */
    private String templateContent;

    /**
     * 模板类型（0-文件，1-目录）
     */
    private Integer templateType;

}