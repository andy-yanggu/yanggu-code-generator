package ${projectPackage}.${projectNameDot}.domain.dto;

<#function importEnumcode formList>
	<#list formList as formField>
		<#if formField.entityBaseField == 0 && formField.enumId??>
			<#return true>
		</#if>
	</#list>
	<#return false>
</#function>
<#assign importEnumcode = importEnumcode(formList)>
import ${projectPackage}.${projectNameDot}.common.validation.group.UpdateGroup;
<#if importEnumcode>
import ${projectPackage}.${projectNameDot}.common.validation.code.EnumCode;
</#if>
<#list formList as formField >
<#if formField.entityBaseField == 0 && formField.enumId??>
import ${projectPackage}.${projectNameDot}.enums.${formField.enumNamePascal}Enum;
</#if>
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
<#list importList as i>
import ${i!};
</#list>

/**
 * ${tableComment}DTO实体类
 */
@Data
@Schema(description = "${tableComment}DTO实体类")
public class ${classNameUpper}DTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

<#list formList as formField>
<#if formField.entityBaseField == 0>
	/**
	 * ${formField.fieldComment}
	 */
	@Schema(description = "${formField.fieldComment}")
	<#if formField.primaryPk == 1>
	@NotNull(message = "${formField.fieldComment}不能为空", groups = {UpdateGroup.class})
	</#if>
	<#if formField.formValidator??>
	@${formField.formValidator}(message = "${formField.fieldComment}不能为空")
	</#if>
	<#if formField.enumId??>
	@EnumCode(${formField.enumNamePascal}Enum.class)
	</#if>
	private ${formField.attrType} ${formField.attrName};

</#if>
</#list>
}
