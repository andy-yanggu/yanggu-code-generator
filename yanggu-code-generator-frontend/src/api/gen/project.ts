import { service } from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/project'

// 项目API
export const genProjectApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 项目下的表
	tableList: (queryForm: any): Promise<any> => {
		return service.post(baseUrl + '/tableList', queryForm)
	}
}
