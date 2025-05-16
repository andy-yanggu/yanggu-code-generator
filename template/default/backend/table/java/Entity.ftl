package ${projectPackage}.${projectNameDot}.domain.entity;

import lombok.Data;
<#if baseClass??>
import lombok.EqualsAndHashCode;
<#else></#if>
import com.baomidou.mybatisplus.annotation.*;
<#if baseClass??>
import ${baseClass.packageName}.${baseClass.code};
</#if>

import java.io.Serial;
import java.io.Serializable;
<#list importList as i>
import ${i!};
</#list>

/**
 * ${tableComment}Entity实体类
 */
@Data
@TableName(value = "${tableName}", schema = "${databaseName}")
<#if baseClass??>
@EqualsAndHashCode(callSuper = true)
<#else></#if>
public class ${classNameUpper}Entity<#if baseClass??> extends ${baseClass.code}</#if> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

<#list fieldList as field>
<#if !field.baseField>
	<#if field.fieldComment!?length gt 0>
	/**
	 * ${field.fieldComment}
	 */
	</#if>
	<#if field.primaryPk>
	@TableId(value = "${field.fieldName}", type = IdType.AUTO)
	<#else>
		<#if field.autoFill == "DEFAULT">
	@TableField(value = "${field.fieldName}")
		</#if>
		<#if field.autoFill == "INSERT">
	@TableField(value = "${field.fieldName}", fill = FieldFill.INSERT)
		</#if>
		<#if field.autoFill == "INSERT_UPDATE">
	@TableField(value = "${field.fieldName}", fill = FieldFill.INSERT_UPDATE)
		</#if>
		<#if field.autoFill == "UPDATE">
	@TableField(value = "${field.fieldName}", fill = FieldFill.UPDATE)
		</#if>
	</#if>
	<#if field.logicDelete>
	@TableLogic(value = "${field.logicNotDeleteValue}", delval = "${field.logicDeleteValue}")
	</#if>
	private ${field.attrType} ${field.attrName};

</#if>
</#list>
}