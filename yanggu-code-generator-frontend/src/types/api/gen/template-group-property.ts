import { Key, LabelData, PageQuery } from '@/types'

/**
 * 模板组属性实体
 */
export interface GenTemplateGroupPropertyEntity {
	// 主键ID
	id: Key
	// 模板组ID
	templateGroupId: number | string
	// 属性标题
	propTitle: string
	// 属性键
	propKey: string
	// 属性默认值
	propDefaultValue: string | number | boolean
	// 组件类型
	componentType: number | string
	// 组件选项
	componentOptions: LabelData[]
	// 必填
	required: number
	// 字段布局方式（1-独占一行，2-一行两个字段）
	columnSpan: number
	// 排序
	propOrder: number
	// 备注
	remark: string
}

/**
 * 模板组属性查询参数
 */
export interface GenTemplateGroupPropertyQuery extends PageQuery {
	// 模板组ID
	templateGroupId?: number | string
	// 属性标题
	propTitle?: string
	// 属性键
	propKey?: string
}
