import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery } from '@/types'
import { service } from '@/utils/request'

// 特定api
interface CustomApi {
	// 变更是否必填
	changeRequired: (id: number | string, required: number) => Promise<void>
	// 变更排序
	changeOrder: (id: number | string, propOrder: number) => Promise<void>
}

const baseUrl: string = '/gen/templateGroupProperty'

// 模板组属性API
export const genTemplateGroupPropertyApi: EnabledCrudApi<GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery>(baseUrl),
	// 变更是否必填
	changeRequired: (id: number | string, required: number) => {
		return service.put(baseUrl + '/changeRequired', { id, required })
	},
	// 变更排序
	changeOrder: (id: number | string, propOrder: number) => {
		return service.put(baseUrl + '/changeOrder', { id, propOrder })
	}
}
