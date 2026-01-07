import { Key, PageQuery } from '@/types'

/**
 * 枚举实体
 */
export interface GenEnumEntity {
	// id
	id: Key
	// 枚举名称
	enumName: string
	// 枚举描述
	enumDesc: string
	// 项目ID
	projectId: number | string
	// 枚举模板组ID
	enumTemplateGroupId?: number | string
	// 枚举项数量
	enumItemCount?: number
	// 枚举模板组属性数据
	templateGroupPropertyData: object
}

/**
 * 枚举查询参数
 */
export interface GenEnumQuery extends PageQuery {
	// 枚举名称
	enumName?: string
	// 项目ID
	projectId?: number | string
}
