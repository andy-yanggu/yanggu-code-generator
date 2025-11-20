import { PageQuery, Key, GenTemplateGroupPropertyEntity } from '@/types'

/**
 * 模板组管理实体
 */
export interface GenTemplateGroupEntity {
	// 主键ID
	id: Key
	// 模板组名称
	groupName: string
	// 模板组类型（0-项目，1-表）
	type: number | string
	// 模板组描述
	groupDesc?: string
	// 模板组属性
	propertyList?: GenTemplateGroupPropertyEntity[]
}

/**
 * 模板组管理查询参数
 */
export interface GenTemplateGroupQuery extends PageQuery {
	// 模板组名称
	groupName?: string
	// 模板组类型（0-项目，1-表）
	type?: number | string
}
