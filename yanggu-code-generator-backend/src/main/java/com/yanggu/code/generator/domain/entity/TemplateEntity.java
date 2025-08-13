package com.yanggu.code.generator.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

import static com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS;

/**
 * 模板Entity实体类
 */
@Data
@TableName(value = "gen_template")
@EqualsAndHashCode(callSuper = true)
public class TemplateEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板组ID
     */
    @TableField(value = "template_group_id")
    private Long templateGroupId;

    /**
     * 父级ID
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 模板名称
     */
    @TableField(value = "template_name")
    private String templateName;

    /**
     * 文件或者目录名称
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 生成代码的路径
     */
    @TableField(value = "generator_path")
    private String generatorPath;

    /**
     * 模板描述
     */
    @TableField(value = "template_desc")
    private String templateDesc;

    /**
     * 模板类型（0-目录，1-模板文件，2-二进制文件）
     */
    @TableField(value = "template_type")
    private Integer templateType;

    /**
     * 模板内容
     */
    @TableField(value = "template_content", updateStrategy = ALWAYS)
    private String templateContent;

    /**
     * 二进制原始文件名
     */
    @TableField(value = "binary_original_file_name")
    private String binaryOriginalFileName;

}