package com.yanggu.code.generator.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;
import com.yanggu.code.generator.common.mybatis.typehandler.map.MapDataTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 枚举Entity实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "gen_enum", autoResultMap = true)
public class EnumEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 枚举名称
     */
    @TableField(value = "enum_name")
    private String enumName;

    /**
     * 枚举描述
     */
    @TableField(value = "enum_desc")
    private String enumDesc;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private Long projectId;

    /**
     * 模板组数据
     */
    @TableField(value = "template_group_property_data", typeHandler = MapDataTypeHandler.class)
    private Map<String, Object> templateGroupPropertyData;

    /**
     * 枚举项列表
     */
    @TableField(exist = false)
    private List<EnumItemEntity> enumItemList;

}