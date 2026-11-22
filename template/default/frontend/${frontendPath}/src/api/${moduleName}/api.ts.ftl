import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { ${moduleNamePascal}${functionNamePascal}Entity, ${moduleNamePascal}${functionNamePascal}Query } from '@/types'


// 特定api
interface CustomApi {
  
}

// 禁用的api
const disabled = [
  
] as const

export type ${moduleNamePascal}${functionNamePascal}ApiType = EnabledCrudApi<${moduleNamePascal}${functionNamePascal}Entity, ${moduleNamePascal}${functionNamePascal}Query, typeof disabled> & CustomApi

const baseUrl: string = '/${moduleNameCase}/${functionName}'

// ${tableComment}API
export const ${moduleNameCase}${functionNamePascal}Api: ${moduleNamePascal}${functionNamePascal}ApiType = {
	// 通用CRUD接口
	...createCrudApi<${moduleNamePascal}${functionNamePascal}Entity, ${moduleNamePascal}${functionNamePascal}Query, typeof disabled>(baseUrl, disabled)
}