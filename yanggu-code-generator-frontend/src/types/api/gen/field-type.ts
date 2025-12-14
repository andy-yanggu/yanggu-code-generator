import { Key, PageQuery } from '@/types'

/**
 * 字段类型管理实体
 */
export interface GenFieldTypeEntity {
	// id
	id?: Key
	// 字段类型
	columnType: string
	// 属性类型
	attrType: string
	// 属性包名
	packageName: string
}

/**
 * 字段类型管理查询参数
 */
export interface GenFieldTypeQuery extends PageQuery {
	// 字段类型
	columnType?: string
	// 属性类型
	attrType?: string
}
