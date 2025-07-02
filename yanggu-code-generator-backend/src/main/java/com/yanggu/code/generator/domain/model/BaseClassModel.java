package com.yanggu.code.generator.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基类Entity实体类
 */
@Data
public class BaseClassModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 基类包名
     */
    private String packageName;

    /**
     * 基类类名
     */
    private String className;

    /**
     * 基类字段，多个用英文逗号分隔
     */
    private String fields;

    /**
     * 备注
     */
    private String remark;

}