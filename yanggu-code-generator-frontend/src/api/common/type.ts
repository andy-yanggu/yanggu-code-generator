// 统一返回数据
export interface Result<T> {
	// 状态码 200为正常
	code: number
	// 提示信息 正常为success
	message: string
	// 数据
	data: T
}

// 排序查询参数
export interface OrderItemQuery {
	// 排序字段
	column: string
	// 是否正序排列，默认 true
	asc: boolean
}

// 统一分页查询参数
export interface PageQuery<T extends Record<string, any> = Record<string, any>> extends T {
	// 当前页码
	pageNum: number
	// 每页条数
	pageSize: number
	// 排序字段
	orderItemList?: OrderItemQuery[]
}

// 分页数据
export interface PageVO<T> {
	// 当前页码
	pageNum: number
	// 每页条数
	pageSize: number
	// 总记录数
	total: number
	// 分页数据
	records: T[]
}
