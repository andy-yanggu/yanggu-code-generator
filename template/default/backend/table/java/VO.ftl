package ${projectPackage}.${projectNameDot}.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import ${projectPackage}.${projectNameDot}.common.domain.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
<#list importList as i>
import ${i!};
</#list>

/**
 * ${tableComment}VO实体类
 */
@Data
@Schema(description = "${tableComment}VO实体类")
@EqualsAndHashCode(callSuper = true)
public class ${classNameUpper}VO extends BaseVO implements Serializable {

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
	<#if field.attrType == 'Date'>
	</#if>
	private ${field.attrType} ${field.attrName};

	</#if>
</#list>
}