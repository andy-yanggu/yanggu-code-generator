// 枚举值类型
export type EnumValueType = string | number | boolean | null

// 枚举标签类型
export type EnumTagTypeValue = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/**
 * 获取表格枚举标签（单独给el-table-column的formatter属性使用）
 */
export const getLabel = (enumList: EnumItem[]) => {
	return (_: any, __: any, value: EnumValueType): string => {
		return <string>getByValue(value, enumList)?.label
	}
}

/**
 * 获取枚举标签label
 */
export const getLabelData = (value: EnumValueType, enumList: EnumItem[], defaultValue = '') => {
	const byValue = getByValue(value, enumList)
	if (!byValue) {
		return defaultValue
	} else {
		return <string>byValue.label
	}
}

/**
 * 根据枚举值获取枚举项
 */
export const getByValue = (value: EnumValueType, enumList: EnumItem[]) => {
	return enumList.find(item => item.value === value)
}

/**
 * 枚举项
 */
export interface EnumItem {
	/**
	 * 枚举值
	 */
	value: EnumValueType

	/**
	 * 枚举标签
	 */
	label: string

	/**
	 * element的type类型
	 */
	type?: EnumTagTypeValue

	/**
	 * 颜色
	 */
	color?: string
}
