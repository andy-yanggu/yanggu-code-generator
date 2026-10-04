import type { FormItemRule } from 'element-plus'
export type { FormItemRule }

// 组件类型（无 el- 前缀，渲染层通过 COMPONENT_MAP 映射）
export type ComponentType =
	| 'input'
	| 'textarea'
	| 'input-number'
	| 'select'
	| 'switch'
	| 'radio'
	| 'checkbox'
	| 'date-picker'
	| 'datetime-picker'
	| 'date-range'
	| 'datetime-range'
	| 'tree-select'
	| 'cascader'

// 表单模式
export type FormMode = 'add' | 'update' | 'copy' | 'detail'

// 分页参数
export interface Pagination {
	current: number
	size: number
	total: number
}

// 搜索字段 schema
export interface SearchFieldSchema {
	/** 字段唯一标识，用于插槽命名和 v-model 绑定；未填时取 prop */
	key?: string
	prop: string
	label: string
	component?: ComponentType
	placeholder?: string
	width?: number | string
	options?: { label: string; value: any }[]
	filterable?: boolean
	rangeFields?: [string, string]
	/** 字段级校验规则 */
	ruleList?: FormItemRule[]
}

// 表单字段 schema
export interface FormFieldSchema {
	/** 字段唯一标识，用于插槽命名；未填时取 prop */
	key?: string
	prop: string
	label: string
	/** label 旁的提示文字，有值时自动渲染问号图标 + tooltip */
	tooltip?: string
	component?: ComponentType
	placeholder?: string
	options?: { label: string; value: any }[]
	filterable?: boolean
	span?: number
	detailFormatter?: (value: any) => string
	/** 字段级校验规则 */
	ruleList?: FormItemRule[]
}

// 表格列 schema
export interface TableColumnSchema {
	key: string
	label: string
	prop?: string
	type?: 'selection' | 'index'
	width?: number | string
	minWidth?: number
	fixed?: 'left' | 'right' | boolean
	align?: 'left' | 'center' | 'right'
	headerAlign?: 'left' | 'center' | 'right'
	sortable?: boolean | 'custom'
	formatter?: (row: any, column?: any, cellValue?: any) => any
	visible?: boolean
	disabled?: boolean
	showOverflowTooltip?: boolean
}

// 归一化搜索字段（补全默认值）
export const normalizeSearchField = (
	field: SearchFieldSchema
): SearchFieldSchema & { key: string; component: ComponentType; placeholder: string; width: number | string } => {
	const component = field.component ?? 'input'
	const placeholder = field.placeholder ?? (component === 'select' ? `请选择${field.label}` : `请输入${field.label}`)
	return {
		...field,
		key: field.key ?? field.prop,
		component,
		placeholder,
		width: field.width ?? 160
	}
}
