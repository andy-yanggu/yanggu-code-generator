// 主键类型
export type Key = string | number

// 主键数组类型
export type KeyArray = Key[]

export interface LabelData {
	// 标签
	label: string
	// 值
	value: string | number | boolean | null
}
