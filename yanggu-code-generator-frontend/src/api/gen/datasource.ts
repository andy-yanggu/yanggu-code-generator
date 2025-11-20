import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenDatasourceEntity, GenDatasourceQuery } from '@/types'

// 特定api
interface CustomApi {
	// 测试数据源接口
	test: (id: number) => Promise<any>
}

type ApiType = EnabledCrudApi<GenDatasourceEntity, GenDatasourceQuery> & CustomApi

const baseUrl: string = '/gen/datasource'

// 数据源API
export const genDatasourceApi: ApiType = {
	// 通用CRUD接口
	...createCrudApi<GenDatasourceEntity, GenDatasourceQuery>(baseUrl),
	// 测试数据源接口
	test: (id: number): Promise<any> => {
		return service.get(baseUrl + '/test', { params: { id } })
	}
}
