/**
 * API 类型统一导出
 */
// common 模块类型
export * from '@/types/api/common'

<#list tableModelList as tableModel>
// ${tableModel.functionNameKebabCase}类型
export * from '@/types/api/${tableModel.moduleName}/${tableModel.functionNameKebabCase}'   
</#list>