/**
 * API 通用类型定义
 */

/**
 * 统一返回数据结构
 */
export interface Result<T = any> {
	// 状态码 200为正常
	code: number
	// 提示信息 正常为success
	message: string
	// 数据
	data: T
}

/**
 * 排序查询参数
 */
export interface OrderItemQuery {
	// 排序字段
	column: string
	// 是否正序排列，默认 true
	asc: boolean
}

/**
 * 分页查询参数基类
 */
export interface PageQuery {
	// 当前页码
	pageNum?: number
	// 每页条数
	pageSize?: number
	// 排序字段
	orderItemList?: OrderItemQuery[]
	// 开始时间
	startTime?: string
	// 结束时间
	endTime?: string
	// 开始日期
	startDate?: string
	// 结束日期
	endDate?: string
	// 时间范围
	dateRange?: string[]
	// 日期范围
	dateTimeRange?: string[]
}

/**
 * 分页返回数据
 */
export interface PageVO<T = any> {
	// 当前页码
	pageNum: number
	// 每页条数
	pageSize: number
	// 总记录数
	total: number
	// 分页数据
	records: T[]
}
