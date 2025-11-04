// 主键类型
export type Key = string | number | null | undefined

// 主键数组类型
export type KeyArray = number[] | string[]

// 标签数据
export interface LabelData {
	// 标签
	label: string
	// 值
	value: string | number | boolean | null
}
