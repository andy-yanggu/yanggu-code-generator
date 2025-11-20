import { PageQuery } from '@/types/api/common'

/**
 * 枚举项实体
 */
export interface GenEnumItemEntity {
	// id
	id: number
	// 枚举ID
	enumId: number
	// 枚举项名称
	enumItemName: string
	// 枚举项编码
	enumItemCode: string
	// 枚举项描述
	enumItemDesc: string
	// 枚举项排序
	enumItemOrder: number
}

/**
 * 枚举项查询参数
 */
export interface GenEnumItemQuery extends PageQuery {
	// 枚举项名称
	enumItemName?: string
	// 枚举ID
	enumId?: number
	// 枚举项编码
	enumItemCode?: string
}
