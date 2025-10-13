package com.yanggu.code.generator.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;
import com.yanggu.code.generator.config.LabelValueDataTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 模板组属性Entity实体类
 */
@Data
@TableName(value = "gen_template_group_property", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class TemplateGroupPropertyEntity extends BaseEntity implements Serializable {

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
	 * 属性标题
	 */
	@TableField(value = "prop_title")
	private String propTitle;

	/**
	 * 属性键
	 */
	@TableField(value = "prop_key")
	private String propKey;

	/**
	 * 属性默认值
	 */
	@TableField(value = "prop_default_value")
	private String propDefaultValue;

	/**
	 * 组件类型，前端显示的组件类型（0-input，1-select、2-radio）
	 */
	@TableField(value = "component_type")
	private Integer componentType;

	/**
	 * 组件选项，多选组件的选项配置
	 */
	@TableField(value = "component_options", typeHandler = LabelValueDataTypeHandler.class)
	private List<LabelValueDataEntity> componentOptions;

	/**
	 * 必填，1：是，0：否
	 */
	@TableField(value = "required")
	private Integer required;

	/**
	 * 排序
	 */
	@TableField(value = "prop_order")
	private Integer propOrder;

	/**
	 * 备注
	 */
	@TableField(value = "remark")
	private String remark;

}
