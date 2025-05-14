package ${projectPackage}.${projectNameUnderline}.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.common.domain.query.PageQuery;
import ${projectPackage}.${projectNameUnderline}.domain.entity.${classNameUpper}Entity;

import java.io.Serial;
import java.io.Serializable;
<#list importList as i>
import ${i!};
</#list>

/**
 * ${tableComment}Entity查询实体类
 */
@Data
@Schema(description = "${tableComment}Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class ${classNameUpper}EntityQuery extends PageQuery<${classNameUpper}Entity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

<#list queryList as field>
    <#if !field.baseField>
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