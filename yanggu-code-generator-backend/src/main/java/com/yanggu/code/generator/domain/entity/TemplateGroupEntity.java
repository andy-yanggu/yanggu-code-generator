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

import static com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS;

/**
 * 模板组Entity实体类
 */
@Data
@TableName(value = "gen_template_group")
@EqualsAndHashCode(callSuper = true)
public class TemplateGroupEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板组名称
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 模板组类型（0-项目，1-表，2-枚举）
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 模板组描述
     */
    @TableField(value = "group_desc", updateStrategy = ALWAYS)
    private String groupDesc;

    /**
     * 模板列表
     */
    @TableField(exist = false)
    private List<TemplateEntity> templateList;

}