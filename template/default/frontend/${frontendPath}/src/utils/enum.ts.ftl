import { EnumItem, EnumValueType } from '@/types'

/**
 * 获取表格枚举标签（单独给el-table-column的formatter属性使用）
 */
export const getLabel = (enumList: EnumItem[]) => {
	return (_: any, __: any, value: EnumValueType): string => {
		return <string>getEnumLabel(value, enumList)
	}
}

/**
 * 获取枚举的label
 */
export const getEnumLabel = (value: EnumValueType, enumList: EnumItem[], defaultLabel = '') => {
	const byValue = getEnumByValue(value, enumList)
	if (!byValue) {
		return defaultLabel
	} else {
		return <string>byValue.label
	}
}

/**
 * 获取枚举的value
 */
export const getEnumValue = (value: EnumValueType, enumList: EnumItem[], defaultValue = '') => {
	const byValue = getEnumByValue(value, enumList)
	if (!byValue) {
		return defaultValue
	} else {
		return <string>byValue.value
	}
}

/**
 * 根据枚举值获取枚举项
 */
export const getEnumByValue = (value: EnumValueType, enumList: EnumItem[]) => {
	return enumList.find(item => item.value === value)
}
