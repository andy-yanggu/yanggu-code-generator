package com.yanggu.code.generator.domain.vo;

import lombok.Data;

/**
 * 预览视图对象
 */
@Data
public class PreviewVO {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板类型（0-文件，1-文件夹）
     */
    private Integer templateType;

    /**
     * 模板组类型（0-项目模板，1-表模板）
     */
    private Integer templateGroupType;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件内容
     */
    private String content;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 表ID
     */
    private Long tableId;

    /**
     * 是否是文件。true-是，false-否
     */
    private Boolean isFile;

}