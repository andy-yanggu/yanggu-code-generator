import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenTemplateGroupCopyDTO, GenTemplateGroupEntity, GenTemplateGroupQuery } from '@/types'

// 特定api
interface CustomApi {
	copy: (dataForm: GenTemplateGroupCopyDTO) => Promise<void>
	cascaderData: (queryForm: any) => Promise<any>
}

const baseUrl: string = '/gen/templateGroup'

// 模板组管理API
export const genTemplateGroupApi: EnabledCrudApi<GenTemplateGroupEntity, GenTemplateGroupQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenTemplateGroupEntity, GenTemplateGroupQuery>(baseUrl),
	// 复制模板组
	copy: (dataForm: GenTemplateGroupCopyDTO) => {
		return service.post(baseUrl + '/copy', dataForm)
	},
	// 获取级联数据
	cascaderData: (queryForm: any): Promise<any> => {
		return service.get(baseUrl + '/cascaderData', { params: queryForm })
	}
}
