import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/baseClass'

export const genBaseClassApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl)
}
