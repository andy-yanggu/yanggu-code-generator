<#list enumDataModelList as enumDataModel>
// ${enumDataModel.fieldComment}枚举
export const ${enumDataModel.tableName}_${enumDataModel.fieldName}_enum = [
<#list enumDataModel.valueList as enumData>
    { label: '${enumData.label}', value: '${enumData.value}' }<#if enumData_has_next>,</#if>
</#list>
]
</#list>