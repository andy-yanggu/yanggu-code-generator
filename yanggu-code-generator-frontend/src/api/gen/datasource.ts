import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenDatasourceEntity, GenDatasourceQuery, DatasourceTestVO } from '@/types'

// 特定api
interface CustomApi {
	// 测试数据源接口
	test: (id: number) => Promise<DatasourceTestVO>
}

type ApiType = EnabledCrudApi<GenDatasourceEntity, GenDatasourceQuery> & CustomApi

const baseUrl: string = '/gen/datasource'

// 数据源API
export const genDatasourceApi: ApiType = {
	// 通用CRUD接口
	...createCrudApi<GenDatasourceEntity, GenDatasourceQuery>(baseUrl),
	// 测试数据源接口
	// noErrorMessage：请求异常统一由页面的测试结果卡片播报，
	// 否则拦截器的 ElMessage 与结果卡片会双份提示同一个错误
	test: (id: number): Promise<DatasourceTestVO> => {
		return service.get(baseUrl + '/test', { params: { id }, noErrorMessage: true })
	}
}
