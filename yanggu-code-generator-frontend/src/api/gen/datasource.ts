import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'

// 数据源管理Entity
export interface GenDatasourceEntity {
	// id
	id: number
	// 数据库类型
	dbType: string
	// 链接名称
	connName: string
	// URL
	connUrl: string
	// 用户名
	username: string
	// 密码
	password: string
	// 描述
	datasourceDesc: string
}

// 数据源管理Query查询参数
export interface GenDatasourceQuery extends PageQuery {
	// 数据库类型
	dbType: string
	// 链接名称
	connName: string
}

// 特定api
interface CustomApi {
	// 测试数据源接口
	test: (id: number) => Promise<any>
}

type ApiType = EnabledCrudApi<GenDatasourceEntity, GenDatasourceQuery> & CustomApi

const baseUrl: string = '/gen/datasource'

// 数据源API
export const genDataSourceApi: ApiType = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 测试数据源接口
	test: (id: number): Promise<any> => {
		return service.get(baseUrl + '/test', { params: { id } })
	}
}
