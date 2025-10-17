import { service } from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/templateGroup'

// 模板组API
export const genTemplateGroupApi = {
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
