package com.yanggu.code.generator.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据源Entity实体类
 */
@Data
@TableName(value = "gen_datasource", schema = "yanggu_code_generator")
@EqualsAndHashCode(callSuper = true)
public class DatasourceEntity extends BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 数据库类型
	 */
	@TableField(value = "db_type")
	private String dbType;

	/**
	 * 连接名
	 */
	@TableField(value = "conn_name")
	private String connName;

	/**
	 * URL
	 */
	@TableField(value = "conn_url")
	private String connUrl;

	/**
	 * 用户名
	 */
	@TableField(value = "username")
	private String username;

	/**
	 * 密码
	 */
	@TableField(value = "password")
	private String password;

	/**
	 * 描述
	 */
	@TableField(value = "data_source_desc")
	private String dataSourceDesc;

}