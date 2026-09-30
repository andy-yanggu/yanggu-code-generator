import type { ColumnConfig, UseTableOptions } from '@/types/hooks/column-config'
import type { Key, KeyArray, PageQuery, PageVO } from '@/types'
import { useTableSettingsStore } from '@/store/table-settings-store'
import { isEmpty, isNotBlank, isNotEmpty } from '@/utils/tool'

/**
 * useTable —— 表格全功能 composable（替代旧 useTableAction）
 *
 * 改进点：
 * - 不再通过 defaultsDeep 运行时变异调用方对象，改为显式 options 入参
 * - 所有内部状态使用 ref，零非空断言
 * - 集成 columns 列配置管理，联动 table-settings-store 持久化
 * - 按职责分段组织：查询/分页、多选、排序、删除、导出/导入、列配置、UI 状态
 */
export const useTable = <VO = any, Query extends PageQuery = PageQuery>(options: UseTableOptions<VO, Query>) => {
	// ====== 配置解构（带默认值） ======
	const primaryKey = (options.primaryKey ?? 'id') as keyof VO
	const isPage = options.isPage !== false
	const mountedGetData = options.mountedGetData !== false
	const resetQueryGetData = options.resetQueryGetData !== false
	const tableSubject = options.tableSubject ?? '数据'
	const defaultPageSizes = options.pageSizes ?? [10, 20, 50, 100, 200]
	const exportSuccessMessage = options.exportSuccessMessage ?? '导出成功，请查看下载的文件'
	const importSuccessMessage = options.importSuccessMessage ?? '导入成功，请查看数据'

	// ====== 1. 查询/分页 ======
	const queryRef = ref()
	const queryForm = reactive({ ...options.initQueryFormData() })
	const dataList = ref([] as VO[])
	const loading = ref(false)
	const pageNum = ref(1)
	const pageSize = ref(defaultPageSizes[0])
	const total = ref(0)
	const order = ref('')
	const asc = ref(false)

	// 构建查询条件
	const buildQueryForm = (): Query => {
		const result: Record<string, any> = {
			...queryForm,
			...(options.queryContext ?? {})
		}

		// dateRange 拆解为 startDate / endDate
		if (result.dateRange && result.dateRange.length === 2) {
			result.startDate = result.dateRange[0]
			result.endDate = result.dateRange[1]
		}
		delete result.dateRange

		// dateTimeRange 拆解为 startTime / endTime
		if (result.dateTimeRange && result.dateTimeRange.length === 2) {
			result.startTime = result.dateTimeRange[0]
			result.endTime = result.dateTimeRange[1]
		}
		delete result.dateTimeRange

		// 分页参数
		if (isPage) {
			result.pageNum = pageNum.value
			result.pageSize = pageSize.value
		}

		// 排序参数
		if (order.value) {
			result.orderItemList = [{ column: order.value, asc: asc.value }]
		}

		return result as Query
	}

	// 执行查询
	const executeQuery = () => {
		loading.value = true
		options
			.dataListApi(buildQueryForm())
			.then(data => {
				if (isPage) {
					const pageVO = data as PageVO<VO>
					dataList.value = pageVO.records
					total.value = pageVO.total
				} else {
					const list = data as VO[]
					dataList.value = list
					total.value = list.length
					pageNum.value = 1
					pageSize.value = list.length
				}
			})
			.finally(() => {
				loading.value = false
			})
	}

	// 表单校验
	const validateQueryForm = (): Promise<void> => {
		return new Promise((resolve, reject) => {
			nextTick(() => {
				// 查询表单隐藏时未渲染，跳过校验
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

	// 查询（带校验）
	const query = () => {
		validateQueryForm()
			.then(() => executeQuery())
			.catch(() => {})
	}

	// 加载数据列表（重置到第一页）
	const getDataList = () => {
		pageNum.value = 1
		query()
	}

	// 重置查询
	const resetQueryHandle = () => {
		nextTick(() => {
			Object.assign(queryForm, options.initQueryFormData())
			if (options.queryContext) {
				Object.assign(queryForm, options.queryContext)
			}
			queryRef.value?.clearValidate()
			if (resetQueryGetData) {
				getDataList()
			}
		})
	}

	// 分页：页码变化
	const currentChangeHandle = (page: number) => {
		pageNum.value = page
		query()
	}

	// 分页：每页条数变化
	const sizeChangeHandle = (size: number) => {
		pageNum.value = 1
		pageSize.value = size
		query()
	}

	// ====== 2. 多选 ======
	const selectedRows = ref([] as VO[])
	const selectedIds = ref([] as KeyArray)

	const selectionChangeHandle = (selections: VO[]) => {
		selectedRows.value = selections
		selectedIds.value = selections.map(item => item[primaryKey]) as KeyArray
	}

	const clearSelectionHandle = () => {
		if (isNotEmpty(selectedIds.value)) {
			nextTick(() => {
				selectedIds.value = []
				selectedRows.value = []
				// 兼容 el-table 和 config-table
				const table = tableRef.value
				table?.clearSelection?.() ?? table?.tableRef?.clearSelection?.()
			})
		}
	}

	// ====== 3. 排序 ======
	const sortChangeHandle = ({ order: sortOrder, prop }: { order: string; prop: string }) => {
		if (sortOrder === 'ascending') {
			order.value = prop
			asc.value = true
		} else if (sortOrder === 'descending') {
			order.value = prop
			asc.value = false
		} else {
			order.value = ''
		}
		getDataList()
	}

	// ====== 4. 批量删除 ======
	const deleteLoading = ref(false)

	// 解析删除上下文（行内删除 / 批量删除）
	const resolveDeleteContext = (arg?: Key | VO) => {
		// 行内删除（row 对象）
		if (arg && typeof arg === 'object') {
			const row = arg as VO
			return { rows: [row], idList: [row[primaryKey]] as KeyArray }
		}
		// 行内删除（id 值）
		if (arg !== undefined) {
			return { rows: [] as VO[], idList: [arg as Key] as KeyArray }
		}
		// 批量删除
		return {
			rows: selectedRows.value,
			idList: selectedIds.value
		}
	}

	// 构建删除文案上下文
	const buildDeleteTextContext = (rows: VO[], idList: KeyArray) => {
		const count = idList.length
		const names: string[] = options.deleteNameKey ? (rows.map(row => row[options.deleteNameKey!] as string).filter(isNotBlank) as string[]) : []

		if (isEmpty(names)) {
			return { count, hasNames: false, nameText: '' }
		} else if (names.length > 3) {
			return { count, hasNames: true, nameText: `【${names.slice(0, 3).join('、')}…】等${count}个` }
		} else {
			return { count, hasNames: true, nameText: `【${names.join('、')}】` }
		}
	}

	const buildDeleteConfirmMessage = (rows: VO[], idList: KeyArray) => {
		if (isNotBlank(options.deleteConfirmMessage)) {
			return options.deleteConfirmMessage
		}
		const ctx = buildDeleteTextContext(rows, idList)
		if (!ctx.hasNames) {
			return ctx.count === 1 ? `确认要删除${tableSubject}吗？` : `确认要删除这${ctx.count}条${tableSubject}吗？`
		} else if (ctx.count <= 3) {
			return `确认要删除${tableSubject}${ctx.nameText}吗？`
		} else {
			return `确认要删除${ctx.nameText}${tableSubject}吗？`
		}
	}

	const buildDeleteSuccessMessage = (rows: VO[], idList: KeyArray) => {
		const ctx = buildDeleteTextContext(rows, idList)
		if (!ctx.hasNames) {
			return ctx.count === 1 ? `${tableSubject}已删除` : `${ctx.count}条${tableSubject}已删除`
		} else if (ctx.count <= 3) {
			return `${tableSubject}${ctx.nameText}已删除`
		} else {
			return `${ctx.nameText}${tableSubject}已删除`
		}
	}

	const deleteBatchHandle = (arg?: Key | VO) => {
		if (!options.deleteListApi) {
			ElMessage.warning('未配置删除接口，请检查')
			return
		}
		const { idList, rows } = resolveDeleteContext(arg)
		const rowsTyped = rows as VO[]
		if (isEmpty(idList)) {
			ElMessage.warning(`请选择要删除的${tableSubject}`)
			return
		}

		ElMessageBox.confirm(buildDeleteConfirmMessage(rowsTyped, idList), '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				deleteLoading.value = true
				options.deleteListApi!(idList)
					.then(() => {
						ElMessage.success({ message: buildDeleteSuccessMessage(rowsTyped, idList), duration: 1000 })
						clearSelectionHandle()
						getDataList()
					})
					.finally(() => {
						deleteLoading.value = false
					})
			})
			.catch(() => {
				ElMessage.info('删除已取消')
			})
	}

	// ====== 5. 导出/导入 ======
	const exportLoading = ref(false)

	const exportHandle = (id?: Key) => {
		if (!options.exportApi) {
			ElMessage.warning('未配置导出接口')
			return
		}
		const idList = (id ? [id] : [...selectedIds.value]) as KeyArray
		if (isEmpty(idList)) {
			ElMessage.warning(`请选择要导出的${tableSubject}`)
			return
		}

		exportLoading.value = true
		options
			.exportApi(idList)
			.then(() => {
				ElMessage.success(exportSuccessMessage)
				clearSelectionHandle()
			})
			.finally(() => {
				exportLoading.value = false
			})
	}

	const importHandle = (file: File, params: Record<string, any> = {}) => {
		if (!file) {
			ElMessage.warning('请选择要导入的文件')
			return
		}
		if (!options.importApi) {
			ElMessage.warning('未配置导入接口')
			return
		}

		const formData = new FormData()
		formData.append('file', file)
		Object.keys(params).forEach(key => formData.append(key, params[key]))

		options.importApi(formData).then(() => {
			ElMessage.success(importSuccessMessage)
			getDataList()
		})
	}

	// ====== 6. 列配置管理 ======
	const settingsStore = useTableSettingsStore()
	// 默认列快照（不克隆，作为重置时的参照源）
	const defaultColumns = options.columns ?? []

	// 浅拷贝列配置（structuredClone 无法克隆 formatter 函数，改用展开运算符）
	const cloneColumns = (cols: ColumnConfig[]): ColumnConfig[] => cols.map(col => ({ ...col }))

	// 以 defaultColumns 为基底，合并 store 中保存的用户设置（visible/fixed/order）
	// 确保 formatter 等不可序列化的属性始终来自默认定义
	const mergeWithDefaults = (saved: ColumnConfig[]): ColumnConfig[] => {
		const defaultMap = new Map(defaultColumns.map(col => [col.key, col]))
		const savedKeys = new Set(saved.map(col => col.key))

		// 按保存的顺序合并，保留默认值中的 formatter 等函数属性
		const merged = saved.map(savedCol => {
			const defaultCol = defaultMap.get(savedCol.key)
			if (!defaultCol) return { ...savedCol }
			return { ...defaultCol, visible: savedCol.visible, fixed: savedCol.fixed }
		})

		// 追加默认中有但保存中没有的新列
		for (const col of defaultColumns) {
			if (!savedKeys.has(col.key)) {
				merged.push({ ...col })
			}
		}

		return merged
	}

	// 从 store 恢复已保存的列设置，否则使用默认值
	const columns = ref<ColumnConfig[]>(
		options.tableKey
			? (settingsStore.getColumns(options.tableKey) ? mergeWithDefaults(settingsStore.getColumns(options.tableKey)!) : cloneColumns(defaultColumns))
			: cloneColumns(defaultColumns)
	)

	// 可见列（过滤掉 visible === false 的列）
	const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

	// 列设置变更后持久化
	const persistColumns = () => {
		if (options.tableKey) {
			settingsStore.saveColumns(options.tableKey, toRaw(columns.value))
		}
	}

	// 重置列设置为默认值
	const resetColumnSettings = () => {
		columns.value = cloneColumns(defaultColumns)
		persistColumns()
	}

	// ====== 7. UI 状态 ======
	const queryShow = ref(true)
	const tableRef = ref()
	const tableCardRef = ref()
	const tableCardMaximized = ref(false)

	// 表格序号列计算（全局序号 = (当前页 - 1) * 每页条数 + 行索引 + 1）
	const tableIndex = (index: number) => {
		return (pageNum.value - 1) * pageSize.value + index + 1
	}

	// ====== 8. 生命周期 ======
	onMounted(() => {
		if (mountedGetData) {
			nextTick(() => {
				resetQueryHandle()
			})
		}
	})

	return {
		// 查询/分页
		dataList,
		loading,
		queryRef,
		queryForm,
		pageNum,
		pageSize,
		total,
		getDataList,
		resetQueryHandle,
		currentChangeHandle,
		sizeChangeHandle,

		// 多选
		selectedRows,
		selectedIds,
		selectionChangeHandle,
		clearSelectionHandle,

		// 排序
		sortChangeHandle,

		// 删除
		deleteBatchHandle,
		deleteLoading,

		// 导出/导入
		exportHandle: exportHandle,
		importHandle,
		exportLoading,

		// 列配置
		columns,
		visibleColumns,
		resetColumnSettings,

		// UI 状态
		queryShow,
		tableRef,
		tableCardRef,
		tableCardMaximized,
		tableIndex
	}
}
