import { service } from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/datasource'

// 数据源API
export const genDataSourceApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 测试数据源接口
	test: (id: number): Promise<any> => {
		return service.get(baseUrl + '/test', { params: { id } })
	}
}
