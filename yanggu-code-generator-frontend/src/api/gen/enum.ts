import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { service } from '@/utils/request'
import { PageQuery } from '@/api/common/type'

// 枚举Entity
export interface GenEnumEntity {
	// id
	id: number
	// 枚举名称
	enumName: string
	// 枚举描述
	enumDesc: string
	// 项目ID
	projectId: number
}

// 枚举Query查询参数
export interface GenEnumQuery extends PageQuery {
	// 枚举名称
	enumName?: string
	// 项目ID
	projectId: number
}

// 特定api
interface CustomApi {
	// 枚举批量生成代码检测
	generateCheck: (idList: number[]) => Promise<any>
}

const baseUrl: string = '/gen/enum'

// 枚举API
export const genEnumApi: EnabledCrudApi<GenEnumEntity, GenEnumQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 枚举批量生成代码检测
	generateCheck: (idList: number[]) => {
		return service.post(baseUrl + '/generateCheck', idList)
	}
}
