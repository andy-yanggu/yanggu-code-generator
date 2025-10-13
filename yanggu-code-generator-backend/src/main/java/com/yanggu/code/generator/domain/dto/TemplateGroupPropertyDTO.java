package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 模板组属性DTO实体类
 */
@Data
@Schema(description = "模板组属性DTO实体类")
public class TemplateGroupPropertyDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@Schema(description = "主键ID")
	@NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
	private Long id;

	/**
	 * 模板组ID
	 */
	@Schema(description = "模板组ID")
	private Long templateGroupId;

	/**
	 * 属性标题
	 */
	@Schema(description = "属性标题")
	private String propTitle;

	/**
	 * 属性键
	 */
	@Schema(description = "属性键")
	private String propKey;

	/**
	 * 属性默认值
	 */
	@Schema(description = "属性默认值")
	private String propDefaultValue;

	/**
	 * 组件类型，前端显示的组件类型（0-input，1-select、2-radio）
	 */
	@Schema(description = "组件类型，前端显示的组件类型（0-input，1-select、2-radio）")
	private Integer componentType;

	/**
	 * 组件选项，多选组件的选项配置
	 */
	@Schema(description = "组件选项，多选组件的选项配置")
	private List<LabelValueDataDTO> componentOptions;

	/**
	 * 必填，1：是，0：否
	 */
	@Schema(description = "必填，1：是，0：否")
	private Integer required;

	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer propOrder;

	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String remark;

}
