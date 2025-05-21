package ${projectPackage}.${projectNameDot}.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

<#list fieldList as field>
<#if field.baseField == 0>
	<#if field.fieldComment!?length gt 0>
	/**
	 * ${field.fieldComment}
	 */
	@Schema(description = "${field.fieldComment}")
	</#if>
	private ${field.attrType} ${field.attrName};

</#if>
</#list>
}