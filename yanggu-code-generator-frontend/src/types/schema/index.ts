import type { FormItemRule } from 'element-plus'

// 组件类型（无 el- 前缀，渲染层通过 COMPONENT_MAP 映射）
export type ComponentType =
	| 'input'
	| 'select'
	| 'textarea'
	| 'radio'
	| 'checkbox'
	| 'switch'
	| 'date-picker'
	| 'date-range'
	| 'datetime-range'
	| 'input-number'
	| 'tree-select'
	| 'cascader'

// 表单模式
export type FormMode = 'add' | 'update' | 'detail'

// 分页参数
export interface Pagination {
	current: number
	size: number
	total: number
}

// 搜索字段 schema
export interface SearchFieldSchema {
	prop: string
	label: string
	component?: ComponentType
	placeholder?: string
	width?: number | string
	options?: { label: string; value: any }[]
	filterable?: boolean
	rangeFields?: [string, string]
}

// 表单字段 schema
export interface FormFieldSchema {
	prop: string
	label: string
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
export const normalizeSearchField = (field: SearchFieldSchema): SearchFieldSchema & { component: ComponentType; placeholder: string; width: number | string } => {
	const component = field.component ?? 'input'
	const placeholder = field.placeholder ?? (component === 'select' ? `请选择${field.label}` : `请输入${field.label}`)
	return {
		...field,
		component,
		placeholder,
		width: field.width ?? 160
	}
}
