import type { Key, KeyArray } from '@/types/common'
import type { MaybeRefOrGetter } from 'vue'
import type { PageVO } from '@/types/api/common'
import type { BuiltinColumnOptions, NormalizedSearchField, Pagination, SearchFieldSchema, SortChangePayload, TableColumnSchema } from '@/types/schema'

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
	/** 固定查询上下文（不被 reset 覆盖，如固定租户 ID）。支持响应式（computed / ref / reactive） */
	queryContext?: MaybeRefOrGetter<Record<string, any>>

	// ======== 分页 ========
	/** 是否需要分页，默认 true。false 时一次返回全部 */
	isPage?: boolean
	/** 每页条数，默认 10 */
	pageSize?: number
	/** 每页条数选项，默认 [10, 20, 50, 100, 200] */
	pageSizes?: number[]
	/** 表格最大高度（CSS 值），默认 '60vh' */
	maxHeight?: string

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
	/** 是否显示工具栏左侧区域（新增/批量删除按钮等），默认 true */
	showToolbarLeft?: boolean
}

/**
 * useCrudTable 返回值契约
 *
 * 存在的理由：组件 props 不能用 `ReturnType<typeof useCrudTable>` 声明。
 * useCrudTable 是泛型函数，`typeof` 取到的是未实例化的签名，ReturnType 只能用默认实参
 * 实例化成 `<any, any>`，与页面侧的具体实体实例化结果比较时会报 TS2719
 * （“Two different types with this name exist, but they are unrelated”）。
 * 具名接口有稳定的类型身份，默认实参下各属性退化为 any，页面传入具体实例化类型仍可直接赋值。
 *
 * 注：各分组由 reactive 包裹，内部 ref / computed 已被自动解包，所以此处均为值类型而非 Ref<T>。
 */
export interface UseCrudTableReturn<VO = any, Query = any> {
	/** 搜索区 */
	search: {
		/** 归一化后的搜索字段 */
		fields: NormalizedSearchField[]
		/** 查询表单（与 initQueryForm 返回值同构） */
		form: Query
		/** 搜索区是否展开 */
		visible: boolean
	}
	/** 查询操作 */
	query: {
		/** 查询（重置到第一页） */
		getDataList: () => void
		/** 重置查询表单 */
		reset: () => void
		onPageChange: (page: number) => void
		onSizeChange: (size: number) => void
		onSortChange: (payload: SortChangePayload) => void
	}
	/** 表格数据 */
	table: {
		data: VO[]
		loading: boolean
		/** isPage 为 false 时为 undefined，DataTable 据此隐藏分页条 */
		pagination?: Pagination
		/** 序号列计算（含分页偏移） */
		index: (index: number) => number
	}
	/** 多选 */
	selection: {
		rows: VO[]
		ids: KeyArray
		onChange: (selections: VO[]) => void
		/** 清空勾选（会同步清掉 el-table 内部的 selection store） */
		clear: () => void
		/** 注入 el-table 实例，由 QueryTable 在 onMounted 里调用 */
		bindTable: (instance: any) => void
	}
	/** 操作（删除 + 导出/导入） */
	action: {
		delete: {
			/** 不传参=批量删除勾选项；传 row=行内删除；传 id=按主键删除 */
			execute: (arg?: Key | VO) => void
			loading: boolean
		}
		export: {
			execute: (id?: Key) => void
			loading: boolean
		}
		import: {
			/** 返回 Promise 以满足 el-upload 的 UploadRequestHandler 契约（需 XMLHttpRequest | Promise<unknown>） */
			execute: (file: File, params?: Record<string, any>) => Promise<void>
		}
	}
	/** 列配置 */
	columnConfig: {
		/** 全量列（含隐藏），v-model 绑定给 DataTable */
		list: TableColumnSchema[]
		/** 恢复默认列配置并持久化 */
		reset: () => void
	}
	/** UI 引用与只读配置 */
	refs: {
		cardMaximized: boolean
		primaryKey: string
		showToolbarLeft: boolean
		/** 每页条数选项，来自 useCrudTable 配置，供分页条使用 */
		pageSizes: number[]
		/** 表格最大高度（CSS 值），来自 useCrudTable 配置 */
		maxHeight: string
	}
}
