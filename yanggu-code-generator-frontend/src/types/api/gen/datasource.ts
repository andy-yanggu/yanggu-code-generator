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
