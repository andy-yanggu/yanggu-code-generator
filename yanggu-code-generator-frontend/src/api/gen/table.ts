import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'
import { KeyArray } from '@/types/common'

// 代码生成表Entity
export interface GenTableEntity {
	// id
	id: number
	// 表名
	tableName: string
	// 数据库名
	databaseName: string
	// 类名
	className: string
	// 说明
	tableComment: string
	// 项目ID
	projectId: number
	// 作者
	author: string
	// 版本
	version: string
	// 模块名称
	moduleName: string
	// 功能名
	functionName: string
	// 表单布局  1：一列   2：两列
	formLayout: number
	// 生成方式
	generatorFunction: number[]
	// 项目名称
	projectName?: string
	// 权限标识
	permissionFlag: string
}

// 代码生成表Query查询参数
export interface GenTableQuery extends PageQuery {
	tableName: string
	projectId: number | undefined
	databaseName: string
	className: string
}

// 特定api
interface CustomApi {
	// 导入表
	import: (dataForm: any) => Promise<void>
	// 同步表
	sync: (id: number) => Promise<void>
	// 表批量生成代码检测
	generateCheck: (idList: KeyArray) => Promise<any>
}

const baseUrl: string = '/gen/table'

// 代码生成表API
export const genTableApi: EnabledCrudApi<GenTableEntity, GenTableQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 导入表
	import: (dataForm: any) => {
		return service.post(baseUrl + '/import', dataForm)
	},
	// 同步表
	sync: (id: number) => {
		return service.put(baseUrl + '/sync', {}, { params: { id } })
	},
	// 表批量生成代码检测
	generateCheck: (idList: KeyArray): Promise<any> => {
		return service.post(baseUrl + '/generateCheck', idList)
	}
}
