package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.yanggu.code.generator.common.domain.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 表字段VO实体类
 */
@Data
@Schema(description = "表字段VO实体类")
@EqualsAndHashCode(callSuper = true)
public class TableFieldVO extends BaseVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@Schema(description = "id")
	private Long id;

	/**
	 * 表ID
	 */
	@Schema(description = "表ID")
	private Long tableId;

	/**
	 * 字段名称
	 */
	@Schema(description = "字段名称")
	private String fieldName;

	/**
	 * 字段类型
	 */
	@Schema(description = "字段类型")
	private String fieldType;

	/**
	 * 字段说明
	 */
	@Schema(description = "字段说明")
	private String fieldComment;

	/**
	 * 属性名
	 */
	@Schema(description = "属性名")
	private String attrName;

	/**
	 * 属性类型
	 */
	@Schema(description = "属性类型")
	private String attrType;

	/**
	 * 属性包名
	 */
	@Schema(description = "属性包名")
	private String packageName;

	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sort;

	/**
	 * 自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE
	 */
	@Schema(description = "自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE")
	private String autoFill;

	/**
	 * 主键 0：否  1：是
	 */
	@Schema(description = "主键 0：否  1：是")
	private Boolean primaryPk;

	/**
	 * 逻辑删除 0：否  1：是
	 */
	@Schema(description = "逻辑删除 0：否  1：是")
	private Boolean logicDelete;

	/**
	 * 逻辑删除值
	 */
	@Schema(description = "逻辑删除值")
	private String logicDeleteValue;

	/**
	 * 逻辑未删除值
	 */
	@Schema(description = "逻辑未删除值")
	private String logicNotDeleteValue;

	/**
	 * 基类字段 0：否  1：是
	 */
	@Schema(description = "基类字段 0：否  1：是")
	private Boolean baseField;

	/**
	 * 表单项 0：否  1：是
	 */
	@Schema(description = "表单项 0：否  1：是")
	private Boolean formItem;

	/**
	 * 表单必填 0：否  1：是
	 */
	@Schema(description = "表单必填 0：否  1：是")
	private Boolean formRequired;

	/**
	 * 表单类型
	 */
	@Schema(description = "表单类型")
	private String formType;

	/**
	 * 表单字典类型
	 */
	@Schema(description = "表单字典类型")
	private String formDict;

	/**
	 * 表单效验
	 */
	@Schema(description = "表单效验")
	private String formValidator;

	/**
	 * 列表项 0：否  1：是
	 */
	@Schema(description = "列表项 0：否  1：是")
	private Boolean gridItem;

	/**
	 * 列表排序 0：否  1：是
	 */
	@Schema(description = "列表排序 0：否  1：是")
	private Boolean gridSort;

	/**
	 * 查询项 0：否  1：是
	 */
	@Schema(description = "查询项 0：否  1：是")
	private Boolean queryItem;

	/**
	 * 查询方式
	 */
	@Schema(description = "查询方式")
	private String queryType;

	/**
	 * 查询表单类型
	 */
	@Schema(description = "查询表单类型")
	private String queryFormType;

}