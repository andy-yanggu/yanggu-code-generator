/** 搜索字段组件类型 */
export type SearchComponent = 'input' | 'select' | 'date-range' | 'datetime-range'

/**
 * 搜索字段配置 —— 独立于表格列定义，驱动 SearchForm 组件
 *
 * 使用方式：
 * ```ts
 * const searchFields: SearchFieldConfig[] = [
 *   { prop: 'name', label: '名称' },
 *   { prop: 'status', label: '状态', component: 'select', options: STATUS_ITEMS },
 *   { prop: 'createTime', label: '创建时间', component: 'date-range', rangeFields: ['createStart', 'createEnd'] },
 * ]
 * ```
 */
export interface SearchFieldConfig {
	/** 绑定到 queryForm 的属性名 */
	prop: string
	/** 表单标签文本 */
	label: string
	/** 组件类型，默认 'input' */
	component?: SearchComponent
	/** 占位符（不传则根据 component 自动生成） */
	placeholder?: string
	/** 输入控件宽度，默认 160 */
	width?: number | string
	/** select 专用：选项列表 */
	options?: { label: string; value: string | number }[]
	/** select 专用：是否可搜索 */
	filterable?: boolean
	/** date-range / datetime-range 专用：拆解后的起止字段名 */
	rangeFields?: [string, string]
}

/**
 * 补全搜索字段的默认值（component / placeholder / width）
 * 页面传入简写定义 → hook 内部归一化为 SearchForm 所需的完整结构
 */
export const normalizeSearchField = (field: SearchFieldConfig): SearchFieldConfig & { component: SearchComponent; placeholder: string; width: number | string } => {
	const component = field.component ?? 'input'
	const placeholder = field.placeholder ?? (component === 'select' ? `请选择${field.label}` : `请输入${field.label}`)
	return {
		...field,
		component,
		placeholder,
		width: field.width ?? 160
	}
}
