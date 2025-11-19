import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'

// 字段类型管理Entity
export interface GenFieldTypeEntity {
	// id
	id: number
	// 字段类型
	columnType: string
	// 属性类型
	attrType: string
	// 属性包名
	packageName: string
}

// 字段类型管理Query查询参数
export interface GenFieldTypeQuery extends PageQuery {
	// 字段类型
	columnType: string
	// 属性类型
	attrType: string
}

// 特定api
interface CustomApi {
	list: () => Promise<string[]>
}

const baseUrl: string = '/gen/fieldType'

// 字段类型管理API
export const genFieldTypeApi: EnabledCrudApi<GenFieldTypeEntity, GenFieldTypeQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenFieldTypeEntity, GenFieldTypeQuery>(baseUrl),
	// 获取字段类型列表
	list: (): Promise<string[]> => {
		return service.get(baseUrl + '/distinctList')
	}
}
