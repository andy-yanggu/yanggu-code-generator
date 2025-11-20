import { EnumItem, EnumTagTypeValue, EnumValueType } from '@/types'

/**
 * 获取表格枚举标签（单独给el-table-column的formatter属性使用）
 */
export const getLabel = (enumList: EnumItem[]) => {
	return (_: any, __: any, value: EnumValueType): string => {
		return <string>getLabelData(value, enumList)
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

export type { EnumValueType, EnumTagTypeValue, EnumItem }
