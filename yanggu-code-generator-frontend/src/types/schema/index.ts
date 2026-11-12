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

// 三视图（Search / Form / Table）共享基类型：身份标识 + 通用展示属性
export interface BaseFieldSchema {
	/** 字段唯一标识，用于插槽命名和 v-model 绑定；未填时取 prop */
	key?: string
	/** 数据字段名；未填时取 key */
	prop?: string
	label: string
	/** 字段提示文字，有值时渲染问号图标 + el-tooltip */
	tooltip?: string
	/** 宽度：搜索/表单字段为输入控件宽度，表格列为列宽 */
	width?: number | string
}

// 输入类字段共享属性（Search + Form 共用，Table 不消费）
export interface InputFieldSchema extends BaseFieldSchema {
	component?: ComponentType
	placeholder?: string
	options?: { label: string; value: any }[]
	clearable?: boolean
	filterable?: boolean
	/** 字段级校验规则 */
	ruleList?: FormItemRule[]
	/**
	 * 日期/时间选择器的默认时刻（映射 el-date-picker 的 default-time）。
	 * - `datetime-picker`：传单个 `Date`，决定选完日期后时间部分的默认值；
	 * - `datetime-range`：传 `[Date, Date]`，分别决定起止时间部分的默认值，
	 *   典型用法 `[00:00:00, 23:59:59]` 以确保后端 `between(createTime, ...)` 覆盖最后一天整天。
	 * 未提供则沿用 Element Plus 默认（起 00:00:00 / 止 00:00:00），可能漏掉末日晚间记录。
	 */
	defaultTime?: Date | [Date, Date]
}

// 搜索字段 schema
export interface SearchFieldSchema extends InputFieldSchema {
	/** 区间选择器的起止字段名（仅搜索使用） */
	rangeFields?: [string, string]
}

// 表单字段 schema
export interface FormFieldSchema extends InputFieldSchema {
	/** 栅格占据列数（仅表单使用） */
	span?: number
	/** 详情模式下的自定义格式化函数（仅表单使用） */
	detailFormatter?: (value: any) => string
	/**
	 * 渲染形态标记（仅表单使用）。
	 * 'bare' = 独占整行的「非数据项」条目（分段标题 / 动态子表单容器等组合布局用的虚拟字段）：
	 * FormField 不套 el-form-item、无 label，内容完全由 form-[key] 插槽提供；未提供插槽则整条不渲染。
	 * 这类条目不对应真实数据列，prop 仅作 v-for key 与插槽名，不触发校验。
	 */
	display?: 'bare'
}

// 表格字段 schema（继承 Base，不继承 Input：表格列不消费输入类属性）
export interface TableFieldSchema extends BaseFieldSchema {
	/** 列唯一标识，用于 v-for key、列设置查找、Sortable 排序（必填） */
	key: string
	prop?: string
	type?: 'selection' | 'index' | 'expand'
	minWidth?: number
	fixed?: 'left' | 'right' | boolean
	align?: 'left' | 'center' | 'right'
	headerAlign?: 'left' | 'center' | 'right'
	sortable?: boolean | 'custom'
	formatter?: (row: any, column?: any, cellValue?: any) => any
	visible?: boolean
	disabled?: boolean
	showOverflowTooltip?: boolean
	/** 多选列是否跨页保持勾选（需配合 el-table row-key 使用） */
	reserveSelection?: boolean
	/**
	 * 多选列的行级勾选开关，映射到 el-table-column 的 selectable（仅 selection 列消费）。
	 * 用于「已导入 / 无权限的行不允许勾选」这类业务态：返回 false 时该行复选框置灰，
	 * 且表头全选会跳过这些行。缺省不下发，等同 el-table 原生行为（全部可勾选）。
	 */
	selectable?: (row: any, index: number) => boolean
	/** 子列；非空即为「分组列」（多级表头父级，仅支持 2 层）。分组列无 prop、不渲染单元格，仅作父表头 */
	children?: TableFieldSchema[]
}

// el-table sort-change 事件负载（order 为 null 表示取消排序）
export interface SortChangePayload {
	prop: string
	order: 'ascending' | 'descending' | null
}

/**
 * 归一化后的搜索字段：key / prop / component / placeholder / width 均已补全。
 * 下游（SearchForm、DynamicField、buildQueryForm）可安全地把这些属性当非空使用，无需 ! 断言。
 */
export type NormalizedSearchField = SearchFieldSchema & {
	key: string
	prop: string
	component: ComponentType
	placeholder: string
	width: number | string
}

/**
 * 归一化后的表单字段：key / prop 均已补全。
 * FormField 用它作索引类型（model[field.prop]），避免 TS2538「undefined 不能作索引类型」。
 */
export type NormalizedFormField = FormFieldSchema & {
	key: string
	prop: string
}

// 区间选择类组件：渲染为 el-date-picker 的 range 形态，使用起止占位符而非 placeholder
const RANGE_COMPONENTS: ComponentType[] = ['date-range', 'datetime-range']

// 搜索字段默认宽度（区间选择器需容纳起止两个值，比普通字段宽）
const DEFAULT_SEARCH_WIDTH: Partial<Record<ComponentType, number>> = {
	'date-range': 260,
	'datetime-range': 380
}

/**
 * key / prop 互为兜底并校验非空。
 *
 * 归一化函数的返回类型承诺 key/prop 是 string，必须在运行时落实：
 * 两者都缺时插槽名会退化成 field-undefined、el-form-item 的 prop 为 undefined
 * 会导致该字段的校验规则静默失效（必填不生效且全程无任何报错）。
 */
const resolveFieldIdentity = (field: SearchFieldSchema | FormFieldSchema) => {
	const key = field.key ?? field.prop
	const prop = field.prop ?? key
	if (!key || !prop) {
		throw new Error(`[crud] 字段缺少 key/prop，无法归一化：${field.label ?? JSON.stringify(field)}`)
	}
	return { key, prop }
}

// 归一化搜索字段（补全默认值）
export const normalizeSearchField = (field: SearchFieldSchema): NormalizedSearchField => {
	const { key, prop } = resolveFieldIdentity(field)
	const component = field.component ?? 'input'
	const isRange = RANGE_COMPONENTS.includes(component)
	const placeholder = field.placeholder ?? (isRange ? '' : component === 'select' ? `请选择${field.label}` : `请输入${field.label}`)
	return {
		...field,
		key,
		prop,
		component,
		placeholder,
		width: field.width ?? DEFAULT_SEARCH_WIDTH[component] ?? 160
	}
}

// 归一化表单字段（补全 prop）
export const normalizeFormField = (field: FormFieldSchema): NormalizedFormField => {
	const { key, prop } = resolveFieldIdentity(field)
	return { ...field, key, prop }
}

// 统一字段声明（一份 fields 派生 formSchema / searchSchema / tableColumns）
export interface CrudField {
	/** 字段唯一标识，用于插槽命名和 v-model 绑定 */
	key: string
	/** 数据字段名；未填时取 key */
	prop?: string
	/** 字段显示名称 */
	label: string
	/** 字段提示文字，有值时渲染问号图标 + el-tooltip（传播到 form/search/table 三处，子对象可覆盖） */
	tooltip?: string

	// ====== 视图角色标记 ======
	/** 是否出现在表单，默认 false */
	inForm?: boolean
	/** 是否出现在查询表单，默认 false */
	inSearch?: boolean
	/** 是否出现在表格列，默认 false */
	inTable?: boolean

	// ====== Form + Search 共享属性（派生时自动注入两个子对象） ======
	/** 组件类型（无 el- 前缀） */
	component?: ComponentType
	/** 选项数据（select / radio / checkbox 等使用） */
	options?: { label: string; value: any }[]
	/** 是否可筛选（select 组件），默认 true */
	filterable?: boolean
	/** 是否可清空（input / select 等），默认 true */
	clearable?: boolean

	// ====== 视图专属子对象（各视图独立覆盖） ======
	/** 表单专属配置（ruleList / tooltip / span 等） */
	form?: Partial<FormFieldSchema>
	/** 查询表单专属配置（width / rangeFields 等） */
	search?: Partial<SearchFieldSchema>
	/** 表格字段专属配置（align / sortable / formatter / minWidth 等） */
	table?: Partial<TableFieldSchema>

	// ====== 多级表头（仅表格视图，2 层） ======
	/**
	 * 子字段，仅用于表格多级表头分组。
	 * 声明 children 的字段本身即「分组节点」：只在表格视图生效（需 inTable: true），不进表单/查询；
	 * children 内的字段照常参与 form/search/table 派生（表单与查询侧会自动拍平，不感知分组）。
	 */
	children?: CrudField[]
}

// 内置列开关（全部默认 true）
export interface BuiltinColumnOptions {
	/** 多选列，默认 true */
	showSelection?: boolean
	/** 序号列，默认 true */
	showIndex?: boolean
	/** 操作列，默认 true */
	showOperation?: boolean
	/** 创建时间列，默认 true */
	showCreateTime?: boolean
	/** 修改时间列，默认 true */
	showUpdateTime?: boolean
}
