import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { IHooksOptions, Key, KeyArray, PageQuery, PageVO } from '@/types'
import { isEmpty, isNotBlank, isNotEmpty } from '@/utils/tool'

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
	// 勾选的表格数据
	const tableRowsRef = ref([] as VO[])

	// 默认值
	const defaultOptions = (): IHooksOptions<VO, Query> => ({
		mountedGetData: true,
		resetQueryGetData: true,
		isPage: true,
		dataListApi: undefined,
		deleteListApi: undefined,
		exportApi: undefined,
		importApi: undefined,
		primaryKey: 'id' as keyof VO,
		queryContext: {} as Query,
		initQueryFormData: () => ({}) as Query,
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
		exportSuccessMessage: '导出成功，请查看下载的文件',
		importSuccessMessage: '导入成功，请查看数据'
	})

	// 合并默认值
	Object.assign(state, Object.fromEntries(Object.entries(defaultOptions()).filter(([key]) => !Object.hasOwn(state, key))))

	// 初始化 queryForm
	if (isEmpty(state.queryForm)) {
		Object.assign(state.queryForm, state.initQueryFormData())
	}

	// 重置后查询
	const resetQueryHandle = () => {
		nextTick(() => {
			// 1️⃣ 先重建查询表单
			Object.assign(state.queryForm, state.initQueryFormData())

			// 2️⃣ 自动合并上下文（无条件）
			if (state.queryContext) {
				Object.assign(state.queryForm, state.queryContext)
			}

			// 3️⃣ 清校验（UI 行为）
			queryRef.value.clearValidate()

			// 4️⃣ 是否查询
			if (state.resetQueryGetData) {
				getDataList()
			}
		})
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
		validateQueryForm()
			.then(() => {
				executeQuery()
			})
			.catch(() => {})
	}

	// 构建查询条件
	const buildQueryForm = (): Query => {
		// 解构查询条件
		const queryForm: Query = {
			// 查询条件
			...(state.queryForm ?? {}),
			// 查询上下文
			...(state.queryContext ?? {})
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
		tableRowsRef.value = selections
		if (!state.primaryKey) {
			console.error('未配置主键')
			return
		}
		state.dataListSelections = selections.map(item => item[state.primaryKey!])
	}

	// 清空勾选
	const clearSelectionHandle = () => {
		if (isNotEmpty(state.dataListSelections)) {
			nextTick(() => {
				state.dataListSelections = []
				tableRowsRef.value = []
				tableRef.value.clearSelection()
			})
		}
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

	// 处理删除上下文
	const resolveDeleteContext = (arg?: Key | VO) => {
		// 行内删除（row）
		if (arg && typeof arg === 'object') {
			const row = arg as VO
			return {
				rows: [row],
				idList: [row[state.primaryKey!] as Key]
			}
		}

		// 行内删除（id）
		if (arg !== undefined) {
			return {
				rows: [],
				idList: [arg as Key]
			}
		}

		// 批量删除
		const rows = (tableRowsRef.value as VO[]) ?? ([] as VO[])
		return {
			rows,
			idList: rows.map(row => row[state.primaryKey!] as Key)
		}
	}

	const getSubject = () => {
		return state.tableSubject ?? '数据'
	}

	const buildDeleteTextContext = (rows: VO[], idList: KeyArray) => {
		const subject = getSubject()
		const count = idList.length

		// 提取名称
		const names: string[] = state.deleteNameKey ? (rows.map(row => row[state.deleteNameKey!]).filter(isNotBlank) as string[]) : []

		// 没有名称
		if (isEmpty(names)) {
			return {
				subject,
				count,
				hasNames: false,
				nameText: ''
			}
		} else if (names.length > 3) {
			// 如果名称大于3个，则只显示前3个名称
			return {
				subject,
				count,
				hasNames: true,
				nameText: `【${names.slice(0, 3).join('、')}…】等${count}个`
			}
		} else {
			return {
				subject,
				count,
				hasNames: true,
				nameText: `【${names.join('、')}】`
			}
		}
	}

	// 构建删除确认文案
	const buildDeleteConfirmMessage = (rows: VO[], idList: KeyArray) => {
		if (isNotBlank(state.deleteConfirmMessage)) {
			return state.deleteConfirmMessage
		}

		const ctx = buildDeleteTextContext(rows, idList)

		// 只有 id（没有名称）
		if (!ctx.hasNames) {
			return ctx.count === 1 ? `确认删除该${ctx.subject}吗？` : `确认删除这${ctx.count}条${ctx.subject}吗？`
		} else if (ctx.count <= 3) {
			return `确认删除${ctx.subject}${ctx.nameText}吗？`
		} else {
			return `确认删除${ctx.nameText}${ctx.subject}吗？`
		}
	}

	// 构建删除成功文案
	const buildDeleteSuccessMessage = (rows: VO[], idList: KeyArray) => {
		const ctx = buildDeleteTextContext(rows, idList)

		// 只有 id（没有名称）
		if (!ctx.hasNames) {
			return ctx.count === 1 ? `${ctx.subject}删除成功` : `${ctx.count}条${ctx.subject}删除成功`
		} else if (ctx.count <= 3) {
			return `${ctx.subject}${ctx.nameText}删除成功`
		} else {
			return `${ctx.nameText}${ctx.subject}删除成功`
		}
	}

	// 批量删除
	const deleteBatchHandle = (arg?: Key | VO) => {
		if (!state.deleteListApi) {
			ElMessage.warning('未配置删除接口，请检查')
			return
		}

		const { idList, rows } = resolveDeleteContext(arg)

		const subject = getSubject()
		if (isEmpty(idList)) {
			ElMessage.warning(`请选择要删除的${subject}`)
			return
		}

		ElMessageBox.confirm(buildDeleteConfirmMessage(rows, idList), '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				state.deleteLoading = true
				state.deleteListApi!(idList)
					.then(() => {
						ElMessage.success({
							message: buildDeleteSuccessMessage(rows, idList),
							duration: 1000
						})
						clearSelectionHandle()
						getDataList()
					})
					.finally(() => {
						state.deleteLoading = false
					})
			})
			.catch(() => {})
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

		if (isEmpty(idList)) {
			ElMessage.warning('请选择要导出的数据')
			return
		}

		state.exportLoading = true
		state.exportApi!(idList)
			.then(() => {
				ElMessage.success(state.exportSuccessMessage)
				// 删除勾选数据
				clearSelectionHandle()
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
		clearSelectionHandle,
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
