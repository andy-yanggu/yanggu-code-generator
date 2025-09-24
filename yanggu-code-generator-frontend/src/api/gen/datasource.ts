import service from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/datasource'

export const genDataSourceApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 测试数据源接口
	test: (id: number) => {
		return service.get(baseUrl + '/test?id=' + id)
	}
}
