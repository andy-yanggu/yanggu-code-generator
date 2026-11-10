import { Key, PageQuery } from '@/types'

/**
 * 数据源管理实体
 */
export interface GenDatasourceEntity {
	// id
	id: Key
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

/**
 * 数据源管理查询参数
 */
export interface GenDatasourceQuery extends PageQuery {
	// 数据库类型
	dbType?: string
	// 链接名称
	connName?: string
}

/**
 * 数据源连接检测结果（对应后端 DatasourceTestVO）
 * 后端把连不上的异常捕获后包装成 result=false 返回，不抛异常，
 * 因此失败原因只能从 errorMessage 取。
 */
export interface DatasourceTestVO {
	// 检测结果
	result: boolean
	// 数据库名称（仅成功时有值）
	databaseName?: string
	// 异常信息（仅失败时有值）
	errorMessage?: string
}
