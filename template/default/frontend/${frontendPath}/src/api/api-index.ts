/**
 * API 统一导出
 *
 * 使用方式：
 * import { authApi } from '@/api'
 */

// 认证模块
export { authApi } from '@/api/auth'

<#list tableModelList as tableModel>
// ${tableModel.functionNameKebabCase}类型
export * from '@/api/${tableModel.moduleName}/${tableModel.functionNameKebabCase}'   
</#list>