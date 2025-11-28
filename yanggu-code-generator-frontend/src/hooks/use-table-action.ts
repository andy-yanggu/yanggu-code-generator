import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { IHooksOptions, Key, KeyArray, PageQuery, PageVO } from '@/types'

// 提供分页、批量删除、导出功能
export const useTableAction = <Query extends PageQuery = PageQuery, VO = any>(state: IHooksOptions<VO, Query>) => {
	// 表单查询引用
	const queryRef = ref()
	// 表单查询是否显示
	const queryShow = ref(true)
	// 表格卡片引用
	const tableCardRef = ref()
	// 表格引用
	const tableRef = ref()

	// 默认值
	const defaultOptions: IHooksOptions<VO, Query> = {
		mountedGetData: true,
		resetQueryGetData: true,
		isPage: true,
		dataListApi: undefined,
		deleteListApi: undefined,
		exportApi: undefined,
		importApi: undefined,
		primaryKey: 'id',
		queryForm: {} as Query,
		dataList: [] as VO[],
		order: '',
		asc: false,
		pageNum: 1,
		pageSize: 10,
		total: 0,
		pageSizes: [10, 20, 50, 100, 200],
		dataListLoading: false,
		exportLoading: false,
		deleteLoading: false,
		dataListSelections: [] as KeyArray,
		deleteConfirmMessage: '确定进行删除操作吗？',
		exportSuccessMessage: '导出成功，请查看下载的文件',
		importSuccessMessage: '导入成功，请查看数据'
	}

	// 合并默认值
	Object.assign(state, Object.fromEntries(Object.entries(defaultOptions).filter(([key]) => !Object.hasOwn(state, key))))

	// 重置后查询
	const resetQueryHandle = () => {
		if (queryRef.value) {
			queryRef.value.clearValidate()
			queryRef.value.resetFields()
		}
		// 查询数据
		if (state.resetQueryGetData) {
			getDataList()
		}
	}

	// 异步验证表单
	const validateQueryForm = (): Promise<void> => {
		return new Promise((resolve, reject) => {
			nextTick(() => {
				// 查询表单隐藏时，查询表单未渲染，需要添加这个判断
				if (!queryShow.value) {
					resolve()
					return
				}
				queryRef.value.validate((valid: boolean) => {
					if (valid) {
						resolve()
					} else {
						reject(new Error('表单校验失败'))
					}
				})
			})
		})
	}

	// 查询
	const query = () => {
		// 先进行表单验证，验证通过后再执行查询
		validateQueryForm().then(() => {
			executeQuery()
		})
	}

	// 构建查询条件
	const buildQueryForm = (): Query => {
		// 解构查询条件
		const queryForm: Query = {
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
			queryForm.pageNum = state.pageNum!
			queryForm.pageSize = state.pageSize!
		}
		// 排序字段
		if (state.order) {
			queryForm.orderItemList = [{ column: state.order, asc: state.asc! }]
		}
		return queryForm
	}

	// 执行实际的查询逻辑
	const executeQuery = () => {
		if (!state.dataListApi) {
			ElMessage.warning('未配置查询接口')
			return
		}

		// 构建查询条件
		const queryForm = buildQueryForm()

		state.dataListLoading = true

		// 调用接口
		state.dataListApi!(queryForm)
			.then(data => {
				if (state.isPage) {
					const pageVO = data as PageVO<VO>
					state.dataList = pageVO.records
					state.total = pageVO.total
				} else {
					const dataList = data as VO[]
					state.dataList = dataList
					state.total = dataList.length
					state.pageNum = 1
					state.pageSize = dataList.length
				}
			})
			.finally(() => {
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
	const selectionChangeHandle = (selections: VO[]) => {
		if (!state.primaryKey) {
			console.error('未配置主键')
			return
		}
		state.dataListSelections = selections.map(item => item[state.primaryKey!])
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
			state.deleteLoading = true
			state.deleteListApi!(idList)
				.then(() => {
					ElMessage.success('删除成功')
					// 删除勾选数据
					if (state.dataListSelections && state.dataListSelections.length > 0) {
						state.dataListSelections = []
						tableRef.value.clearSelection()
					}
					getDataList()
				})
				.finally(() => {
					state.deleteLoading = false
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
			ElMessage.warning('请选择要导出的数据')
			return
		}

		state.exportLoading = true
		state.exportApi!(idList)
			.then(() => {
				ElMessage.success(state.exportSuccessMessage)
				// 删除勾选数据
				if (state.dataListSelections && state.dataListSelections.length > 0) {
					state.dataListSelections = []
					tableRef.value.clearSelection()
				}
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

	// 生命周期函数
	onMounted(() => {
		if (state.mountedGetData) {
			nextTick(() => {
				resetQueryHandle()
			})
		}
	})

	return {
		getDataList,
		sizeChangeHandle,
		currentChangeHandle,
		selectionChangeHandle,
		sortChangeHandle,
		deleteBatchHandle,
		queryRef,
		queryShow,
		tableCardRef,
		tableRef,
		resetQueryHandle,
		tableIndex,
		exportHandle,
		importHandle
	}
}
