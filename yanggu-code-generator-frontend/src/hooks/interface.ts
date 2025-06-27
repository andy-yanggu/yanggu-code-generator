type DataListApi<T = any> = (params: any) => Promise<T>
type DeleteListApi<T = any> = (params: any) => Promise<T>

export interface IHooksOptions {
	// 否在创建页面时，调用数据列表接口
	createdIsNeed?: boolean
	// 是否需要分页
	isPage?: boolean
	// 数据列表接口
	dataListApi?: DataListApi
	// 删除接口
	deleteListApi?: DeleteListApi
	// 主键key，用于删除场景
	primaryKey?: string
	// 导出 Url
	exportUrl?: string
	// 查询条件
	queryForm?: any
	// 数据列表
	dataList?: any[]
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
	// 数据列表，多选项
	dataListSelections?: any[]
}
