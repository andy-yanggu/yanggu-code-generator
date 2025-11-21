import { KeyArray, PageQuery, PageVO } from '@/types'

// 数据列表接口
type DataListApi<Query, VO> = (queryForm?: Query) => Promise<PageVO<VO> | VO[]>
// 批量删除接口
type DeleteListApi = (idList: KeyArray) => Promise<void>
// 导出接口
type ExportApi = (idList: KeyArray) => Promise<void>
// 导入接口
type ImportApi = (formData: FormData) => Promise<void>

// 表格操作
export interface IHooksOptions<Query extends PageQuery = PageQuery, VO = any> {
	// 否在创建页面时，调用数据列表接口
	mountedGetData?: boolean
	// 重置后是否查询
	resetQueryGetData?: boolean
	// 是否需要分页
	isPage?: boolean
	// 数据列表接口
	dataListApi?: DataListApi<Query, VO>
	// 删除接口
	deleteListApi?: DeleteListApi
	// 导出接口
	exportApi?: ExportApi
	// 导入接口
	importApi?: ImportApi
	// 主键key，用于删除场景
	primaryKey?: string
	// 查询条件
	queryForm: Query
	// 数据列表
	dataList?: VO[]
	// 排序字段
	order?: string
	// 是否升序
	asc?: boolean
	// 当前页码
	pageNum?: number
	// 每页数
	pageSize?: number
	// 总条数
	total?: number
	// 每页数选择项
	pageSizes?: number[]
	// 数据列表，loading状态
	dataListLoading?: boolean
	// 删除loading状态
	deleteLoading?: boolean
	// 导出loading状态
	exportLoading?: boolean
	// 数据列表，多选项
	dataListSelections?: KeyArray
	// 删除时提示语
	deleteConfirmMessage?: string
	// 导出时提示语
	exportSuccessMessage?: string
	// 导入成功提示语
	importSuccessMessage?: string
}
