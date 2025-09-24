import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/enumItem'

export const genEnumItemApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl)
}
