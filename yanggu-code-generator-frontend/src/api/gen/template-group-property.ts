import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/templateGroupProperty'

// 模板组属性API
export const genTemplateGroupPropertyApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl)
}