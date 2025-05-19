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

/**
 * 项目Entity实体类
 */
@Data
@TableName(value = "gen_project")
@EqualsAndHashCode(callSuper = true)
public class ProjectEntity extends BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * ID主键自增
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 项目名称
	 */
	@TableField(value = "project_name")
	private String projectName;

	/**
	 * 项目包名
	 */
	@TableField(value = "project_package")
	private String projectPackage;

	/**
	 * 项目版本
	 */
	@TableField(value = "project_version")
	private String projectVersion;

	/**
	 * 数据源ID
	 */
	@TableField(value = "datasource_id")
	private Long datasourceId;

	/**
	 * 项目模板组ID
	 */
	@TableField(value = "project_template_group_id")
	private Long projectTemplateGroupId;

	/**
	 * 表模板组ID
	 */
	@TableField(value = "table_template_group_id")
	private Long tableTemplateGroupId;

	/**
	 * 后端路径
	 */
	@TableField(value = "backend_path")
	private String backendPath;

	/**
	 * 前端路径
	 */
	@TableField(value = "frontend_path")
	private String frontendPath;

	/**
	 * 项目描述
	 */
	@TableField(value = "project_desc")
	private String projectDesc;

	/**
	 * 作者
	 */
	@TableField(value = "author")
	private String author;

	/**
	 * 基类ID
	 */
	@TableField(value = "base_class_id")
	private Long baseClassId;

	/**
	 * 生成方式（0-zip压缩包，1-服务器本地）
	 */
	@TableField(value = "generator_type")
	private Integer generatorType;

}