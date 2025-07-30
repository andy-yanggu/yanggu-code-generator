import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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
	// 删除时提示语
	deleteMessage?: string
}

export const useIndexQuery = (options: IHooksOptions) => {
	// 表单查询引用
	const queryRef = ref()

	// 重置查询
	const resetQueryHandle = () => {
		if (queryRef.value) {
			queryRef.value.resetFields()
		}
	}

	const defaultOptions: IHooksOptions = {
		dataListApi: () => Promise.resolve({ data: [] }),
		deleteListApi: () => Promise.resolve(),
		createdIsNeed: true,
		isPage: true,
		primaryKey: 'id',
		queryForm: {},
		dataList: [],
		order: '',
		asc: false,
		pageNum: 1,
		pageSize: 10,
		total: 0,
		pageSizes: [10, 20, 50, 100, 200],
		dataListLoading: false,
		dataListSelections: [],
		deleteMessage: '确定进行删除操作?'
	}

	const mergeDefaultOptions = (options: any, props: any): IHooksOptions => {
		for (const key in options) {
			if (!Object.getOwnPropertyDescriptor(props, key)) {
				props[key] = options[key]
			}
		}
		return props
	}

	// 覆盖默认值
	const state = mergeDefaultOptions(defaultOptions, options)

	// 创建完毕
	onMounted(() => {
		if (state.createdIsNeed) {
			query()
		}
	})

	const query = () => {
		if (!state.dataListApi) {
			return
		}

		//解构查询条件
		const queryForm = {
			...state.queryForm
		}
		//如果queryForm包含dateRange
		if (queryForm.hasOwnProperty('dateRange')) {
			if (queryForm.dateRange && queryForm.dateRange.length === 2) {
				queryForm.startDate = queryForm.dateRange[0]
				queryForm.endDate = queryForm.dateRange[1]
			}
			delete queryForm.dateRange
		}

		//如果queryForm包含dateTimeRange
		if (queryForm.hasOwnProperty('dateTimeRange')) {
			if (queryForm.dateTimeRange && queryForm.dateTimeRange.length === 2) {
				queryForm.startTime = queryForm.dateTimeRange[0]
				queryForm.endTime = queryForm.dateTimeRange[1]
			}
			delete queryForm.dateTimeRange
		}

		//如果是分页，添加分页参数
		if (state.isPage) {
			queryForm.pageNum = state.pageNum
			queryForm.pageSize = state.pageSize
		}

		//排序字段
		if (state.order) {
			queryForm.orderItemList = [{ column: state.order, asc: state.asc }]
		}

		state.dataListLoading = true

		//调用接口
		state.dataListApi(queryForm).then((res: any) => {
			if (state.isPage) {
				state.dataList = res.data.records
				state.total = res.data.total
			} else {
				state.dataList = res.data
				state.total = res.data.length
				state.pageNum = 1
				state.pageSize = res.data.length
			}
			state.dataListLoading = false
		})
	}

	// 加载数据列表
	const getDataList = () => {
		state.pageNum = 1
		query()
	}

	// pageNum发生变化
	const currentChangeHandle = (pageNum: number) => {
		state.pageNum = pageNum
		query()
	}

	// pageSize发生变化
	const sizeChangeHandle = (pageSize: number) => {
		state.pageNum = 1
		state.pageSize = pageSize
		query()
	}

	// 多选
	const selectionChangeHandle = (selections: any[]) => {
		state.dataListSelections = selections.map((item: any) => state.primaryKey && item[state.primaryKey])
	}

	// 排序
	const sortChangeHandle = ({ order, prop }: any) => {
		if (order === 'ascending') {
			state.order = prop
			state.asc = true
		} else if (order === 'descending') {
			state.order = prop
			state.asc = false
		} else {
			state.order = ''
		}
		getDataList()
	}

	// 批量删除
	const deleteBatchHandle = (key?: number | string) => {
		let data: any[] = []
		if (key) {
			data = [key]
		} else {
			data = state.dataListSelections ? state.dataListSelections : []

			if (data.length === 0) {
				ElMessage.warning('请选择删除记录')
				return
			}
		}

		ElMessageBox.confirm(state.deleteMessage, '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				state.deleteListApi!(data).then(() => {
					ElMessage.success('删除成功')
					query()
				})
			})
			.catch(() => {})
	}

	const tableIndex = (index: number) => {
		return (state.pageNum! - 1) * state.pageSize! + index + 1
	}

	return {
		getDataList,
		sizeChangeHandle,
		currentChangeHandle,
		selectionChangeHandle,
		sortChangeHandle,
		deleteBatchHandle,
		queryRef,
		resetQueryHandle,
		tableIndex
	}
}
