import { PageQuery } from '@/types/api/common'
import { Key } from '@/types'

/**
 * 枚举项实体
 */
export interface GenEnumItemEntity {
	// id
	id: Key
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
	// 标签类型(primary/success/info/warning/danger)，未配置为 null
	tagType?: string | null
	// 标签样式(light/dark/plain)，未配置为 null
	tagEffect?: string | null
	// 标签背景色(hex，如#409EFF)，未配置为 null
	tagColor?: string | null
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
