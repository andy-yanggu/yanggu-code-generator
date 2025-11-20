import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenEnumItemEntity, GenEnumItemQuery } from '@/types'

// 特定api
interface CustomApi {}

const baseUrl: string = '/gen/enumItem'

// 枚举项API
export const genEnumItemApi: EnabledCrudApi<GenEnumItemEntity, GenEnumItemQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenEnumItemEntity, GenEnumItemQuery>(baseUrl)
}
