import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/enumItem'

// 枚举项API
export const genEnumItemApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl)
}
