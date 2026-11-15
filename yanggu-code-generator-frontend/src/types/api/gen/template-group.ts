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
 * 模板组复制请求参数
 *
 * 复制走专用接口，后端靠 oldGroupId 定位被复制的源模板组；
 * 表单里的 id 在 copy 模式下会被清空（CRUD 通用的复制语义），所以源 ID 必须单独传
 */
export interface GenTemplateGroupCopyDTO extends GenTemplateGroupEntity {
	// 被复制的原模板组ID
	oldGroupId: Key
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
