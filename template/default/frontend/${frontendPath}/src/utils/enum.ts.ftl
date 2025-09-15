/**
 * 获取枚举标签
 */
export const getLabel = (value: any, enumList: EnumItem[]): string => {
	return <string>enumList.find(item => item.value === value)?.label
}

/**
 * 枚举项
 */
export interface EnumItem {
	/**
	 * 枚举值
	 */
	value: any

	/**
	 * 枚举标签
	 */
	label: string
}
