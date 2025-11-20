import { Key, PageQuery } from '@/types'

/**
 * 基类实体
 */
export interface GenBaseClassEntity {
	// 主键ID
	id: Key
	// 基类名称
	baseClassName: string
	// 基类包名
	packageName: string
	// 基类类名
	className: string
	// 基类字段
	fields: string
	// 备注
	remark: string
}

/**
 * 基类查询参数
 */
export interface GenBaseClassQuery extends PageQuery {
	// 基类包名
	packageName?: string
	// 基类类名
	className?: string
}
