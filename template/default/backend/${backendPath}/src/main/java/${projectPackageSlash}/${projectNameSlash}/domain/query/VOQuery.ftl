package ${projectPackage}.${projectNameDot}.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ${projectPackage}.${projectNameDot}.common.domain.query.PageQuery;
import ${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO;

import java.io.Serial;
import java.io.Serializable;
<#list importList as i>
import ${i!};
</#list>

/**
 * ${tableComment}VO查询实体类
 */
@Data
@Schema(description = "${tableComment}VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class ${classNameUpper}VOQuery extends PageQuery<${classNameUpper}VO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

<#list queryList as field>
    <#if field.fieldComment!?length gt 0>
    /**
     * ${field.fieldComment}
     */
    @Schema(description = "${field.fieldComment}")
    </#if>
    <#if field.attrType == 'Date'>
    </#if>
    private ${field.attrType} ${field.attrName};

</#list>
}
