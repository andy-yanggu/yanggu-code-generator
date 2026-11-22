/**
 * 工具类型定义
 */

/**
 * 枚举值类型
 */
export type EnumValueType = string | number | boolean | null

/**
 * 枚举标签类型值（Element Plus tag 组件类型）
 */
export type EnumTagTypeValue = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/**
 * 枚举项接口
 * 用于构建选择器、单选框、多选框等组件的数据源
 */
export interface EnumItem {
	/** 枚举值 */
	value: EnumValueType
	/** 枚举标签 */
	label: string
	/** Element Plus 的 type 类型（用于标签组件） */
	type?: EnumTagTypeValue
	/** 颜色 */
	color?: string
}
