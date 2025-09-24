import service from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/tableField'

export const genTableFieldApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 提交列表
	submitList: (dataFormList: any[]) => {
		return service.post(baseUrl + '/submitList', dataFormList)
	}
}
