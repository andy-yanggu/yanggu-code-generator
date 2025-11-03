import { service } from '@/utils/request'
import { createCrudApi, CrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'

// 模板组管理Entity
export interface GenTemplateGroupEntity {
	// 主键ID
	id: number
	// 模板组名称
	groupName: string
	// 模板组类型（0-项目，1-表）
	type: number | undefined
	// 模板组描述
	groupDesc: string
}

// 模板组管理Query查询参数
export interface GenTemplateGroupQuery extends PageQuery {
	// 模板组名称
	groupName: string
	// 模板组类型（0-项目，1-表）
	type: number | undefined
}

// 特定api
interface CustomApi {
	copy: (dataForm: any) => Promise<void>
	cascaderData: (queryForm: any) => Promise<any>
}

const baseUrl: string = '/gen/templateGroup'

// 模板组管理API
export const genTemplateGroupApi: CrudApi<GenTemplateGroupEntity, GenTemplateGroupQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 复制模板组
	copy: (dataForm: any) => {
		return service.post(baseUrl + '/copy', dataForm)
	},
	// 获取级联数据
	cascaderData: (queryForm: any): Promise<any> => {
		return service.get(baseUrl + '/cascaderData', { params: queryForm })
	}
}
