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
import java.util.List;

/**
 * 表Entity实体类
 */
@Data
@TableName(value = "gen_table", schema = "yanggu_code_generator")
@EqualsAndHashCode(callSuper = true)
public class TableEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 表名
     */
    @TableField(value = "table_name")
    private String tableName;

    /**
     * 数据库名
     */
    @TableField(value = "database_name")
    private String databaseName;

    /**
     * 类名
     */
    @TableField(value = "class_name")
    private String className;

    /**
     * 说明
     */
    @TableField(value = "table_comment")
    private String tableComment;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private Long projectId;

    /**
     * 作者
     */
    @TableField(value = "author")
    private String author;

    /**
     * 版本
     */
    @TableField(value = "version")
    private String version;

    /**
     * 功能名
     */
    @TableField(value = "function_name")
    private String functionName;

    /**
     * 表单布局  1：一列   2：两列
     */
    @TableField(value = "form_layout")
    private Integer formLayout;

    /**
     * 表字段列表
     */
    @TableField(exist = false)
    private List<TableFieldEntity> tableFieldList;

}