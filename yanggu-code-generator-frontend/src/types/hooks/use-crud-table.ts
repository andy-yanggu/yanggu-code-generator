import type { Key } from '@/types/common'
import type { PageVO } from '@/types/api/common'
import type { BuiltinColumnOptions, SearchFieldSchema, TableColumnSchema } from '@/types/schema'

// 数据列表接口
type DataListApi<VO, Query> = (query: Query) => Promise<PageVO<VO>>
// 批量删除接口
type DeleteListApi = (idList: Key[]) => Promise<void>
// 导出接口
type ExportApi = (idList: Key[]) => Promise<void>
// 导入接口
type ImportApi = (formData: FormData) => Promise<void>

/**
 * useCrudTable 配置选项
 *
 * 设计原则：配置只包含「声明式」内容（接口引用、字段定义、文案、开关），
 * 运行时状态（dataList、pageNum、loading 等）全部由 hook 内部管理。
 */
export interface UseCrudTableOptions<VO = any, Query = any> extends BuiltinColumnOptions {
	// ======== 必填 ========
	/** 分页/列表查询接口 */
	dataListApi: DataListApi<VO, Query>
	// ======== 表格列（使用 fields 模式时可省略，由 useCrud 自动派生） ========
	/** 表格列配置 */
	tableColumns?: TableColumnSchema[]
	// ======== 查询 ========
	/** 查询表单初始值工厂；无搜索表单时无需填写，默认 () => ({}) */
	initQueryForm?: () => Query
	/** 搜索字段配置 */
	searchSchema?: SearchFieldSchema[]
	/** 固定查询上下文（不被 reset 覆盖，如固定租户 ID） */
	queryContext?: Record<string, any>

	// ======== 分页 ========
	/** 是否需要分页，默认 true。false 时一次返回全部 */
	isPage?: boolean
	/** 每页条数，默认 10 */
	pageSize?: number
	/** 每页条数选项，默认 [10, 20, 50, 100, 200] */
	pageSizes?: number[]

	// ======== 删除 ========
	/** 批量删除接口 */
	deleteApi?: DeleteListApi
	/** 删除确认时展示的名称字段 */
	deleteNameKey?: string
	/** 自定义删除确认文案（优先级最高） */
	deleteConfirmMessage?: string

	// ======== 导出/导入 ========
	/** 导出接口 */
	exportApi?: ExportApi
	/** 导入接口 */
	importApi?: ImportApi
	/** 导出成功提示，默认 '导出成功，请查看下载的文件' */
	exportSuccessMessage?: string
	/** 导入成功提示，默认 '导入成功，请查看数据' */
	importSuccessMessage?: string

	// ======== 基础标识 ========
	/** 主键字段名，默认 'id' */
	primaryKey?: string
	/** 主体名称，默认 '数据'。用于删除/导出提示文案 */
	subject?: string
	/** 列配置持久化 key，不传则不持久化 */
	tableKey?: string

	// ======== 行为开关 ========
	/** 是否在 mounted 时自动加载，默认 true */
	mountedGetData?: boolean
	/** 重置查询表单后是否自动查询，默认 true */
	resetQueryGetData?: boolean
}
