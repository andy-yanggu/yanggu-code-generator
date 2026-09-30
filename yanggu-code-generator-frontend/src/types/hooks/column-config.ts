import type { KeyArray } from '@/types/common'
import type { PageQuery, PageVO } from '@/types/api/common'
import type { SearchFieldConfig } from '@/types/hooks/search-field'

/**
 * 列配置 —— 定义表格列的元数据
 *
 * key 同时作为：唯一标识、命名插槽名、store 持久化 key
 *
 * 使用方式：
 * ```ts
 * const columns: ColumnConfig[] = [
 *   { key: 'selection', type: 'selection', width: 50, fixed: 'left', disabled: true },
 *   { key: 'name', prop: 'name', label: '名称', minWidth: 120 },
 *   { key: 'operation', label: '操作', fixed: 'right', width: 150, disabled: true },
 * ]
 * ```
 */
export interface ColumnConfig {
	/** 唯一标识，同时作为命名插槽名 */
	key: string
	/** 列标题 */
	label: string
	/** 列属性字段名（selection/index 等特殊列无 prop） */
	prop?: string
	/** 列类型 */
	type?: 'selection' | 'index'

	// ---- ElTableColumn 透传属性 ----
	/** 列宽度 */
	width?: number | string
	/** 列最小宽度 */
	minWidth?: number
	/** 固定列 */
	fixed?: 'left' | 'right' | boolean
	/** 列内容对齐方式 */
	align?: 'left' | 'center' | 'right'
	/** 列头对齐方式 */
	headerAlign?: 'left' | 'center' | 'right'
	/** 是否可排序 */
	sortable?: boolean | 'custom'
	/** 是否超出时显示 tooltip */
	showOverflowTooltip?: boolean
	/** 格式化单元格内容（与 ElTableColumn formatter 签名一致） */
	formatter?: (row: any, column?: any, cellValue?: any) => any

	// ---- 列设置控制 ----
	/** 是否显示，默认 true */
	visible?: boolean
	/** 是否在列设置面板中禁用（不可隐藏/拖动），固定列建议设为 true */
	disabled?: boolean
}

// ---- useTable 类型定义 ----

/** 数据列表接口 */
type DataListApi<Query, VO> = (queryForm?: Query) => Promise<PageVO<VO> | VO[]>
/** 批量删除接口 */
type DeleteListApi = (idList: KeyArray) => Promise<void>
/** 导出接口 */
type ExportApi = (idList: KeyArray) => Promise<void>
/** 导入接口 */
type ImportApi = (formData: FormData) => Promise<void>

/**
 * useTable 配置选项
 *
 * 替代旧的 IHooksOptions，采用显式 options 入参（不再通过 defaultsDeep 运行时变异调用方对象）
 */
export interface UseTableOptions<VO = any, Query extends PageQuery = PageQuery> {
	// ---- 必填 ----
	/** 数据列表查询接口 */
	dataListApi: DataListApi<Query, VO>

	// ---- 查询/分页 ----
	/** 表格主体名称，用于删除/导出提示语，如：'用户'、'角色' */
	tableSubject?: string
	/** 初始化 / 重置查询条件 */
	initQueryFormData: () => Query
	/** 查询上下文（不会被 reset 覆盖的固定参数） */
	queryContext?: Partial<Query>
	/** 是否在 mounted 时自动加载数据，默认 true */
	mountedGetData?: boolean
	/** 重置后是否自动查询，默认 true */
	resetQueryGetData?: boolean
	/** 是否需要分页，默认 true */
	isPage?: boolean
	/** 每页条数选项，默认 [10, 20, 50, 100, 200] */
	pageSizes?: number[]

	// ---- 列配置 ----
	/** 列配置数组 */
	columns?: ColumnConfig[]
	/** 用于 store 持久化的唯一 key，不传则不持久化列设置 */
	tableKey?: string

	// ---- 搜索表单（独立于列配置） ----
	/** 搜索字段定义数组，与表格列完全解耦 */
	searchFields?: SearchFieldConfig[]

	// ---- 删除 ----
	/** 批量删除接口 */
	deleteListApi?: DeleteListApi
	/** 主键字段名，用于删除/多选场景，默认 'id' */
	primaryKey?: keyof VO
	/** 删除确认时用于展示的名称字段 */
	deleteNameKey?: keyof VO
	/** 自定义删除确认提示语 */
	deleteConfirmMessage?: string

	// ---- 导出/导入 ----
	/** 导出接口 */
	exportApi?: ExportApi
	/** 导入接口 */
	importApi?: ImportApi
	/** 导出成功提示语 */
	exportSuccessMessage?: string
	/** 导入成功提示语 */
	importSuccessMessage?: string
}
