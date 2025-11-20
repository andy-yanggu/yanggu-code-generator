import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenProjectEntity, GenProjectQuery } from '@/types'

// 特定api
interface CustomApi {
	// 项目下的表
	tableList: (queryForm: any) => Promise<any>
}

const baseUrl: string = '/gen/project'

// 项目API
export const genProjectApi: EnabledCrudApi<GenProjectEntity, GenProjectQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenProjectEntity, GenProjectQuery>(baseUrl),
	// 项目下的表
	tableList: (queryForm: any): Promise<any> => {
		return service.post(baseUrl + '/tableList', queryForm)
	}
}
