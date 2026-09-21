import type { AnyEnum } from 'enum-plus'

/**
 * 获取表格枚举标签（单独给el-table-column的formatter属性使用）
 */
export const getLabel = (enumObj: AnyEnum) => {
	return (_: any, __: any, value: any): string => {
		return enumObj.label(value) ?? ''
	}
}
