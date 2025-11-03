import { createCrudApi, CrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'

// 枚举项Entity
export interface GenEnumItemEntity {
	// id
	id: number
	// 枚举ID
	enumId: number
	// 枚举项名称
	enumItemName: string
	// 枚举项编码
	enumItemCode: string
	// 枚举项描述
	enumItemDesc: string
	// 枚举项排序
	enumItemOrder: number
}

// 枚举项Query查询参数
export interface GenEnumItemQuery extends PageQuery {
	// 枚举项名称
	enumItemName: string
	// 枚举ID
	enumId: number
	// 枚举项编码
	enumItemCode: string
}

// 特定api
interface CustomApi {}

const baseUrl: string = '/gen/enumItem'

// 枚举项API
export const genEnumItemApi: CrudApi<GenEnumItemEntity, GenEnumItemQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl)
}
