import type { Key } from '@/types/common'
import type { PageVO } from '@/types/api/common'
import type { TableColumnSchema, SearchFieldSchema } from '@/types/schema'

// 数据列表接口
type DataListApi<VO, Query> = (query: Query) => Promise<PageVO<VO>>
// 批量删除接口
type DeleteListApi = (idList: Key[]) => Promise<void>

/**
 * useCrudTable 配置选项
 */
export interface UseCrudTableOptions<VO = any, Query = any> {
	// ---- 必填 ----
	/** 分页查询接口 */
	dataListApi: DataListApi<VO, Query>
	/** 查询表单初始值工厂 */
	initQueryForm: () => Query
	/** 表格列配置 */
	columns: TableColumnSchema[]

	// ---- 搜索 ----
	/** 搜索字段配置 */
	searchSchema?: SearchFieldSchema[]

	// ---- 分页 ----
	/** 每页条数，默认 10 */
	pageSize?: number
	/** 每页条数选项 */
	pageSizes?: number[]

	// ---- 删除 ----
	/** 批量删除接口 */
	deleteApi?: DeleteListApi
	/** 主键字段名，默认 'id' */
	primaryKey?: string
	/** 主体名称，默认 '数据' */
	subject?: string
	/** 删除确认时展示的名称字段 */
	deleteNameKey?: string

	// ---- 列设置 ----
	/** 持久化 key */
	tableKey?: string

	// ---- 行为 ----
	/** 是否在 mounted 时自动加载，默认 true */
	mountedGetData?: boolean
	/** 重置后是否自动查询，默认 true */
	resetQueryGetData?: boolean
	/** 固定查询上下文（不被 reset 覆盖） */
	queryContext?: Record<string, any>
}
