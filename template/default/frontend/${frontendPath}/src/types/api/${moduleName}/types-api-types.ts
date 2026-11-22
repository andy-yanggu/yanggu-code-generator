import { Key, PageQuery } from '@/types'
<#function getJSType attrType>
    <#if attrType == "Integer" || attrType == "int" ||
         attrType == "Long" || attrType == "long">
        <#return "number | string">
    <#elseif attrType == "Float" || attrType == "float" ||
             attrType == "Double" || attrType == "double">
        <#return "number | string">
    <#elseif attrType == "String" || attrType == "char" || attrType == "Character">
        <#return "string">
    <#elseif attrType == "Boolean" || attrType == "boolean">
        <#return "boolean | string">
    <#else>
        <#return "string">
    </#if>
</#function>

// ${tableComment}Entity
export interface ${moduleNamePascal}${functionNamePascal}Entity {
<#list fieldList as field>
  <#if field.entityBaseField == 0>
  // ${field.fieldComment}
  ${field.attrName}: ${getJSType(field.attrType)}
  </#if>
</#list>
}

// ${tableComment}Query查询参数
export interface ${moduleNamePascal}${functionNamePascal}Query extends PageQuery {
<#list queryList as field>
  <#if field.entityBaseField == 0>
  // ${field.fieldComment}
  ${field.attrName}: ${getJSType(field.attrType)}
  </#if>
</#list>
}