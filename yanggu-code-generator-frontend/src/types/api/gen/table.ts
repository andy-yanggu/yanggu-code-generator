import { Key, PageQuery } from '@/types'

/**
 * 代码生成表实体
 */
export interface GenTableEntity {
	// id
	id: Key
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
	formLayout: number | string
	// 弹窗方式（0-对话框，1-抽屉）
	popupType: number | string
	// 表项目模板组数据
	templateGroupPropertyData: object
	// 生成方式
	generatorFunction: number[]
	// Entity基类ID
	entityBaseClassId: number | string
	// VO基类ID
	voBaseClassId: number | string
	// 项目名称
	projectName?: string
	// 权限标识
	permissionFlag: string
	// 表项目模板组ID
	tableTemplateGroupId: number | string
}

/**
 * 代码生成表查询参数
 */
export interface GenTableQuery extends PageQuery {
	tableName?: string
	projectId?: number | string
	databaseName?: string
	className?: string
}
