import type { ColumnConfig, UseTableOptions } from '@/types/hooks/column-config'
import type { SearchFieldConfig } from '@/types/hooks/search-field'
import type { Key, KeyArray, PageQuery, PageVO } from '@/types'
import { normalizeSearchField } from '@/types/hooks/search-field'
import { useTableSettingsStore } from '@/store/table-settings-store'
import { isEmpty, isNotBlank, isNotEmpty } from '@/utils/tool'

/**
 * useTableV2 —— 表格全功能 composable（useTable 的增强版）
 *
 * 相比 useTable 的改进：
 * - 返回值按语义分组（query / table / selection / delete / exportImport / columns）
 * - 接收独立的 searchFields 定义，驱动 SearchForm 组件（与表格列完全解耦）
 * - buildQueryForm 支持 date-range 字段自动拆解为起止字段
 * - 仍保留平铺字段返回，兼容旧页面渐进迁移
 */
export const useTableV2 = <VO = any, Query extends PageQuery = PageQuery>(options: UseTableOptions<VO, Query>) => {
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

	// 归一化搜索字段（补全 component / placeholder / width 默认值）
	const searchFields = computed(() => (options.searchFields ?? []).map(normalizeSearchField))

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

		// searchFields 中的 date-range / datetime-range 字段拆解
		for (const field of searchFields.value) {
			if (field.component === 'date-range' || field.component === 'datetime-range') {
				const val = result[field.prop]
				if (Array.isArray(val) && val.length === 2) {
					const [startKey, endKey] = field.rangeFields ?? [`${field.prop}Start`, `${field.prop}End`]
					result[startKey] = val[0]
					result[endKey] = val[1]
				}
				delete result[field.prop]
			}
		}

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
				if (!queryShow.value) {
					resolve()
					return
				}
				// queryRef 指向 SearchForm 内部的 el-form，若页面未通过 ref 暴露则跳过校验（由 SearchForm 内部处理）
				if (!queryRef.value) {
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

	const resolveDeleteContext = (arg?: Key | VO) => {
		if (arg && typeof arg === 'object') {
			const row = arg as VO
			return { rows: [row], idList: [row[primaryKey]] as KeyArray }
		}
		if (arg !== undefined) {
			return { rows: [] as VO[], idList: [arg as Key] as KeyArray }
		}
		return {
			rows: selectedRows.value,
			idList: selectedIds.value
		}
	}

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
	const defaultColumns = options.columns ?? []

	const cloneColumns = (cols: ColumnConfig[]): ColumnConfig[] => cols.map(col => ({ ...col }))

	const mergeWithDefaults = (saved: ColumnConfig[]): ColumnConfig[] => {
		const defaultMap = new Map(defaultColumns.map(col => [col.key, col]))
		const savedKeys = new Set(saved.map(col => col.key))

		const merged = saved.map(savedCol => {
			const defaultCol = defaultMap.get(savedCol.key)
			if (!defaultCol) return { ...savedCol }
			return { ...defaultCol, visible: savedCol.visible, fixed: savedCol.fixed }
		})

		for (const col of defaultColumns) {
			if (!savedKeys.has(col.key)) {
				merged.push({ ...col })
			}
		}

		return merged
	}

	const columns = ref<ColumnConfig[]>(
		options.tableKey
			? (settingsStore.getColumns(options.tableKey) ? mergeWithDefaults(settingsStore.getColumns(options.tableKey)!) : cloneColumns(defaultColumns))
			: cloneColumns(defaultColumns)
	)

	const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

	const persistColumns = () => {
		if (options.tableKey) {
			settingsStore.saveColumns(options.tableKey, toRaw(columns.value))
		}
	}

	const resetColumnSettings = () => {
		columns.value = cloneColumns(defaultColumns)
		persistColumns()
	}

	// ====== 7. UI 状态 ======
	const queryShow = ref(true)
	const tableRef = ref()
	const tableCardRef = ref()
	const tableCardMaximized = ref(false)

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

	// ====== 返回值：分组（reactive 自动解包嵌套 ref）+ 平铺（兼容旧页面） ======
	return {
		// ---- 分组返回（新页面推荐，reactive 包裹使模板中嵌套 ref 自动解包） ----
		/** 搜索表单（状态独立，不与 query/table 混杂） */
		search: reactive({
			fields: searchFields,
			form: queryForm,
			visible: queryShow
		}),

		/** 查询操作（纯动作，不含状态） */
		query: reactive({
			ref: queryRef,
			getDataList,
			reset: resetQueryHandle,
			onPageChange: currentChangeHandle,
			onSizeChange: sizeChangeHandle,
			onSortChange: sortChangeHandle
		}),

		/** 表格数据 */
		table: reactive({
			data: dataList,
			loading,
			ref: tableRef,
			cardRef: tableCardRef,
			cardMaximized: tableCardMaximized,
			pagination: computed(() => ({ current: pageNum.value, size: pageSize.value, total: total.value })),
			index: tableIndex
		}),

		/** 多选 */
		selection: reactive({
			rows: selectedRows,
			ids: selectedIds,
			onChange: selectionChangeHandle,
			clear: clearSelectionHandle
		}),

		/** 删除 */
		delete: reactive({
			execute: deleteBatchHandle,
			loading: deleteLoading
		}),

		/** 导出/导入 */
		exportImport: reactive({
			export: exportHandle,
			import: importHandle,
			exportLoading
		}),

		/** 列配置 */
		columnConfig: reactive({
			list: columns,
			visible: visibleColumns,
			reset: resetColumnSettings
		}),

		// ---- 平铺返回（兼容旧页面，迁移完成后删除） ----
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
		selectedRows,
		selectedIds,
		selectionChangeHandle,
		clearSelectionHandle,
		sortChangeHandle,
		deleteBatchHandle,
		deleteLoading,
		exportHandle,
		importHandle,
		exportLoading,
		columns,
		visibleColumns,
		resetColumnSettings,
		queryShow,
		tableRef,
		tableCardRef,
		tableCardMaximized,
		tableIndex
	}
}
