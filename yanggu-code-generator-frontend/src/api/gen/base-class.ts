import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/baseClass'

// 基类API
export const genBaseClassApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl)
}
