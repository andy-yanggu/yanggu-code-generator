import type { UseCrudTableOptions } from '@/types/hooks/use-crud-table'
import type { TableColumnSchema, Pagination } from '@/types/schema'
import { normalizeSearchField } from '@/types/schema'
import type { Key, KeyArray } from '@/types/common'
import type { PageVO } from '@/types/api/common'
import { useTableSettingsStore } from '@/store/table-settings-store'
import { isEmpty, isNotBlank, isNotEmpty } from '@/utils/tool'

export const useCrudTable = <VO = any>(options: UseCrudTableOptions<VO>) => {
	// ====== 配置解构 ======
	const primaryKey = options.primaryKey ?? 'id'
	const subject = options.subject ?? '数据'
	const defaultPageSizes = options.pageSizes ?? [10, 20, 50, 100, 200]
	const mountedGetData = options.mountedGetData !== false
	const resetQueryGetData = options.resetQueryGetData !== false

	// ====== 1. 查询/分页 ======
	const queryForm = reactive({ ...options.initQueryForm() })
	const dataList = ref([] as VO[])
	const loading = ref(false)
	const pageNum = ref(1)
	const pageSize = ref(options.pageSize ?? defaultPageSizes[0])
	const total = ref(0)
	const order = ref('')
	const asc = ref(false)

	// 归一化搜索字段
	const searchFields = computed(() => (options.searchSchema ?? []).map(normalizeSearchField))

	// 构建查询条件
	const buildQueryForm = () => {
		const result: Record<string, any> = {
			...queryForm,
			...(options.queryContext ?? {})
		}

		// searchSchema 中的 date-range / datetime-range 字段拆解
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
		result.pageNum = pageNum.value
		result.pageSize = pageSize.value

		// 排序参数
		if (order.value) {
			result.orderItemList = [{ column: order.value, asc: asc.value }]
		}

		return result
	}

	// 执行查询
	const executeQuery = () => {
		loading.value = true
		options
			.dataListApi(buildQueryForm())
			.then((data: PageVO<VO>) => {
				dataList.value = data.records
				total.value = data.total
			})
			.finally(() => {
				loading.value = false
			})
	}

	// 查询（重置到第一页）
	const getDataList = () => {
		pageNum.value = 1
		executeQuery()
	}

	// 重置查询
	const resetQueryHandle = () => {
		nextTick(() => {
			Object.assign(queryForm, options.initQueryForm())
			if (options.queryContext) {
				Object.assign(queryForm, options.queryContext)
			}
			if (resetQueryGetData) {
				getDataList()
			}
		})
	}

	// 分页：页码变化
	const onPageChange = (page: number) => {
		pageNum.value = page
		executeQuery()
	}

	// 分页：每页条数变化
	const onSizeChange = (size: number) => {
		pageNum.value = 1
		pageSize.value = size
		executeQuery()
	}

	// 排序变化
	const onSortChange = ({ order: sortOrder, prop }: { order: string; prop: string }) => {
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

	// ====== 2. 多选 ======
	const selectedRows = ref([] as VO[])
	const selectedIds = ref([] as KeyArray)

	const onSelectionChange = (selections: VO[]) => {
		selectedRows.value = selections
		selectedIds.value = selections.map(item => (item as any)[primaryKey]) as KeyArray
	}

	const clearSelection = () => {
		if (isNotEmpty(selectedIds.value)) {
			nextTick(() => {
				selectedIds.value = []
				selectedRows.value = []
			})
		}
	}

	// ====== 3. 批量删除 ======
	const deleteLoading = ref(false)

	const resolveDeleteContext = (arg?: Key | VO) => {
		if (arg && typeof arg === 'object') {
			const row = arg as VO
			return { rows: [row], idList: [(row as any)[primaryKey]] as KeyArray }
		}
		if (arg !== undefined) {
			return { rows: [] as VO[], idList: [arg as Key] as KeyArray }
		}
		return { rows: selectedRows.value, idList: selectedIds.value }
	}

	const buildDeleteConfirmMessage = (rows: VO[], idList: KeyArray) => {
		const count = idList.length
		const names: string[] = options.deleteNameKey
			? (rows.map(row => (row as any)[options.deleteNameKey!] as string).filter(isNotBlank) as string[])
			: []

		if (isEmpty(names)) {
			return count === 1 ? `确认要删除${subject}吗？` : `确认要删除这${count}条${subject}吗？`
		} else if (names.length <= 3) {
			return `确认要删除${subject}【${names.join('、')}】吗？`
		} else {
			return `确认要删除【${names.slice(0, 3).join('、')}…】等${count}个${subject}吗？`
		}
	}

	const deleteExecute = (arg?: Key | VO) => {
		if (!options.deleteApi) {
			ElMessage.warning('未配置删除接口，请检查')
			return
		}
		const { idList, rows } = resolveDeleteContext(arg)
		if (isEmpty(idList)) {
			ElMessage.warning(`请选择要删除的${subject}`)
			return
		}

		ElMessageBox.confirm(buildDeleteConfirmMessage(rows as VO[], idList), '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				deleteLoading.value = true
				options
					.deleteApi!(idList)
					.then(() => {
						ElMessage.success('删除成功')
						clearSelection()
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

	// ====== 4. 列配置管理 ======
	const settingsStore = useTableSettingsStore()
	const defaultColumns = options.columns ?? []

	const cloneColumns = (cols: TableColumnSchema[]): TableColumnSchema[] => cols.map(col => ({ ...col }))

	const mergeWithDefaults = (saved: TableColumnSchema[]): TableColumnSchema[] => {
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

	const columns = ref<TableColumnSchema[]>(
		options.tableKey
			? settingsStore.getColumns(options.tableKey)
				? mergeWithDefaults(settingsStore.getColumns(options.tableKey)!)
				: cloneColumns(defaultColumns)
			: cloneColumns(defaultColumns)
	)

	const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

	const persistColumns = () => {
		if (options.tableKey) {
			settingsStore.saveColumns(options.tableKey, toRaw(columns.value) as any)
		}
	}

	const resetColumnSettings = () => {
		columns.value = cloneColumns(defaultColumns)
		persistColumns()
	}

	// ====== 5. UI 状态 ======
	const queryShow = ref(true)
	const cardMaximized = ref(false)

	const tableIndex = (index: number) => {
		return (pageNum.value - 1) * pageSize.value + index + 1
	}

	// ====== 6. 生命周期 ======
	onMounted(() => {
		if (mountedGetData) {
			nextTick(() => {
				resetQueryHandle()
			})
		}
	})

	// ====== 返回值：7 组 reactive 包裹 ======
	return {
		search: reactive({
			fields: searchFields,
			form: queryForm,
			visible: queryShow
		}),
		query: reactive({
			getDataList,
			reset: resetQueryHandle,
			onPageChange,
			onSizeChange,
			onSortChange
		}),
		table: reactive({
			data: dataList,
			loading,
			pagination: computed<Pagination>(() => ({ current: pageNum.value, size: pageSize.value, total: total.value })),
			index: tableIndex
		}),
		selection: reactive({
			rows: selectedRows,
			ids: selectedIds,
			onChange: onSelectionChange,
			clear: clearSelection
		}),
		delete: reactive({
			execute: deleteExecute,
			loading: deleteLoading
		}),
		columnConfig: reactive({
			list: columns,
			visible: visibleColumns,
			reset: resetColumnSettings
		}),
		refs: reactive({
			cardMaximized
		})
	}
}
