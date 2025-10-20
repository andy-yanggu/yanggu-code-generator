import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Key, KeyArray } from '@/types/common'

// 数据列表接口
type DataListApi<T = any> = (params: any) => Promise<T>
// 批量删除接口
type DeleteListApi<T = any> = (idList: KeyArray) => Promise<T>
// 导出接口
type ExportApi<T = any> = (idList: KeyArray) => Promise<T>
// 导入接口
type ImportApi<T = any> = (formData: FormData) => Promise<T>

export interface IHooksOptions {
	// 否在创建页面时，调用数据列表接口
	createdIsNeed?: boolean
	// 是否需要分页
	isPage?: boolean
	// 数据列表接口
	dataListApi?: DataListApi
	// 删除接口
	deleteListApi?: DeleteListApi
	// 导出接口
	exportApi?: ExportApi
	// 导入接口
	importApi?: ImportApi
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

// 提供分页、批量删除、导出功能
const useTableAction = (options: IHooksOptions) => {
	// 表单查询引用
	const queryRef = ref()
	// 表格引用
	const tableRef = ref()

	// 重置查询
	const resetQueryHandle = () => {
		if (queryRef.value) {
			queryRef.value.clearValidate()
			queryRef.value.resetFields()
		}
	}

	// 默认值
	const defaultOptions: IHooksOptions = {
		dataListApi: undefined,
		deleteListApi: undefined,
		exportApi: undefined,
		importApi: undefined,
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
		exportLoading: false,
		dataListSelections: [],
		deleteConfirmMessage: '确定进行删除操作？',
		exportSuccessMessage: '导出成功，请查看下载的文件',
		importSuccessMessage: '导入成功，请查看数据'
	}

	// 合并默认值
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

	// 执行实际的查询逻辑
	const executeQuery = () => {
		if (!state.dataListApi) {
			ElMessage.warning('未配置查询接口')
			return
		}
		// 解构查询条件
		const queryForm = {
			...state.queryForm
		}
		// 如果queryForm包含dateRange
		if (queryForm.hasOwnProperty('dateRange')) {
			if (queryForm.dateRange && queryForm.dateRange.length === 2) {
				queryForm.startDate = queryForm.dateRange[0]
				queryForm.endDate = queryForm.dateRange[1]
			}
			delete queryForm.dateRange
		}

		// 如果queryForm包含dateTimeRange
		if (queryForm.hasOwnProperty('dateTimeRange')) {
			if (queryForm.dateTimeRange && queryForm.dateTimeRange.length === 2) {
				queryForm.startTime = queryForm.dateTimeRange[0]
				queryForm.endTime = queryForm.dateTimeRange[1]
			}
			delete queryForm.dateTimeRange
		}

		// 如果是分页，添加分页参数
		if (state.isPage) {
			queryForm.pageNum = state.pageNum
			queryForm.pageSize = state.pageSize
		}

		// 排序字段
		if (state.order) {
			queryForm.orderItemList = [{ column: state.order, asc: state.asc }]
		}

		state.dataListLoading = true

		// 调用接口
		state.dataListApi!(queryForm)
			.then(data => {
				if (state.isPage) {
					state.dataList = data.records
					state.total = data.total
				} else {
					state.dataList = data
					state.total = data.length
					state.pageNum = 1
					state.pageSize = data.length
				}
			})
			.finally(() => {
				state.dataListLoading = false
			})
	}

	// 异步验证表单
	const validateQueryForm = (): Promise<boolean> => {
		return new Promise(resolve => {
			if (queryRef.value) {
				queryRef.value.validate((valid: boolean) => {
					resolve(valid)
				})
			} else {
				resolve(true)
			}
		})
	}

	// 查询
	const query = () => {
		// 先进行表单验证，验证通过后再执行查询
		validateQueryForm().then(valid => {
			if (!valid) {
				return
			}
			executeQuery()
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
		if (!state.primaryKey) {
			console.error('未配置主键')
			return
		}
		state.dataListSelections = selections.map((item: any) => item[state.primaryKey!])
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
	const deleteBatchHandle = (id?: Key) => {
		if (!state.deleteListApi) {
			ElMessage.warning('未配置删除接口')
			return
		}
		const idList = (id ? [id] : [...(state.dataListSelections ?? [])]) as KeyArray

		if (idList.length === 0) {
			ElMessage.warning('请选择删除记录')
			return
		}

		ElMessageBox.confirm(state.deleteConfirmMessage, '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		}).then(() => {
			state.deleteListApi!(idList).then(() => {
				ElMessage.success('删除成功')
				query()
			})
		})
	}

	// 表格序号列计算
	const tableIndex = (index: number) => {
		return (state.pageNum! - 1) * state.pageSize! + index + 1
	}

	// 勾选导出和单个导出
	const exportHandle = (id?: Key) => {
		if (!state.exportApi) {
			ElMessage.warning('未配置导出接口')
			return
		}

		const idList = (id ? [id] : [...(state.dataListSelections ?? [])]) as KeyArray

		if (idList.length === 0) {
			ElMessage.warning('请勾选要导出的数据')
			return
		}

		state.exportLoading = true
		state.exportApi!(idList)
			.then(() => {
				ElMessage.success(state.exportSuccessMessage)
				tableRef.value.clearSelection()
				state.dataListSelections = []
			})
			.finally(() => {
				state.exportLoading = false
			})
	}

	// 导入数据
	const importHandle = (file: File, params: Record<string, any> = {}) => {
		if (!file) {
			ElMessage.warning('请选择要导入的文件')
			return
		}

		if (!state.importApi) {
			ElMessage.warning('未配置导入接口')
			return
		}
		// 表单参数
		const formData = new FormData()
		// 文件
		formData.append('file', file)
		// 添加其他参数
		Object.keys(params).forEach(key => formData.append(key, params[key]))

		// 调用导入接口
		state.importApi!(formData)
			.then(() => {
				ElMessage.success(state.importSuccessMessage)
			})
			.then(() => {
				getDataList()
			})
	}

	return {
		getDataList,
		sizeChangeHandle,
		currentChangeHandle,
		selectionChangeHandle,
		sortChangeHandle,
		deleteBatchHandle,
		queryRef,
		tableRef,
		resetQueryHandle,
		tableIndex,
		exportHandle,
		importHandle
	}
}
export default useTableAction
