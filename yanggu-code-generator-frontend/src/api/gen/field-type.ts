import service from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/fieldType'

export const genFieldTypeApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 获取字段类型列表
	list: () => {
		return service.get(baseUrl + '/distinctList')
	}
}
