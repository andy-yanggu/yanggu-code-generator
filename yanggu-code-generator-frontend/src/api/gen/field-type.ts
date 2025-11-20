import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenFieldTypeEntity, GenFieldTypeQuery } from '@/types'

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
