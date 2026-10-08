import type { UseCrudTableOptions } from '@/types/hooks/use-crud-table'
import type { Pagination, TableColumnSchema } from '@/types/schema'
import { normalizeSearchField } from '@/types/schema'
import type { Key, KeyArray } from '@/types/common'
import type { PageVO } from '@/types/api/common'
import { useTableSettingsStore } from '@/store/table-settings-store'
import { defaultsDeep, isEmpty, isNotBlank, isNotEmpty } from '@/utils/tool'
import { toValue } from 'vue'

// ======== 内置固定列 ========
export const BUILTIN_SELECTION: TableColumnSchema = {
	key: 'selection',
	type: 'selection',
	label: '',
	width: 50,
	align: 'center',
	fixed: 'left',
	disabled: true,
	reserveSelection: true
}
export const BUILTIN_INDEX: TableColumnSchema = { key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true }
export const BUILTIN_CREATE_TIME: TableColumnSchema = {
	key: 'createTime',
	label: '创建时间',
	minWidth: 120,
	sortable: 'custom',
	align: 'center',
	showOverflowTooltip: true
}
export const BUILTIN_UPDATE_TIME: TableColumnSchema = {
	key: 'updateTime',
	label: '修改时间',
	minWidth: 120,
	sortable: 'custom',
	align: 'center',
	showOverflowTooltip: true
}
export const BUILTIN_OPERATION: TableColumnSchema = { key: 'operation', label: '操作', fixed: 'right', width: 150, align: 'center', disabled: true }

export const useCrudTable = <VO = any, Query = any>(opts: UseCrudTableOptions<VO, Query>) => {
	// ====== 默认值（仅填充缺失属性，保留 reactive 响应性） ======
	const defaultOptions = (): Partial<UseCrudTableOptions<VO, Query>> => ({
		primaryKey: 'id',
		subject: '数据',
		isPage: true,
		pageSizes: [10, 20, 50, 100, 200],
		pageSize: 10,
		mountedGetData: true,
		resetQueryGetData: true,
		initQueryForm: () => ({}) as Query,
		searchSchema: [],
		queryContext: {},
		exportSuccessMessage: '导出成功，请查看下载的文件',
		importSuccessMessage: '导入成功，请查看数据'
	})

	defaultsDeep(opts, defaultOptions())

	// ====== 配置解构 ======
	const primaryKey = opts.primaryKey!
	const subject = opts.subject!
	const isPage = opts.isPage!
	const mountedGetData = opts.mountedGetData!
	const resetQueryGetData = opts.resetQueryGetData!

	// ====== 1. 查询/分页 ======
	const queryForm = reactive({ ...(opts.initQueryForm!() || {}) })
	const dataList = ref([] as VO[])
	const loading = ref(false)
	const pageNum = ref(1)
	const pageSize = ref(opts.pageSize!)
	const total = ref(0)
	const order = ref('')
	const asc = ref(false)

	// 归一化搜索字段
	const searchFields = computed(() => opts.searchSchema!.map(normalizeSearchField))

	// 构建查询条件
	const buildQueryForm = () => {
		const result: Record<string, any> = {
			...queryForm,
			...toValue(opts.queryContext)
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
		if (isPage) {
			result.pageNum = pageNum.value
			result.pageSize = pageSize.value
		}

		// 排序参数
		if (order.value) {
			result.orderItemList = [{ column: order.value, asc: asc.value }]
		}

		return result
	}

	// 执行查询
	const executeQuery = () => {
		loading.value = true
		opts
			.dataListApi(buildQueryForm() as Query)
			.then((data: PageVO<VO> | VO[]) => {
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

	// 查询（重置到第一页）
	const getDataList = () => {
		pageNum.value = 1
		executeQuery()
	}

	// 重置查询
	const resetQueryHandle = () => {
		nextTick(() => {
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

	// ====== 3. 操作（删除 + 导出/导入） ======
	const deleteLoading = ref(false)
	const exportLoading = ref(false)

	// --- 删除 ---
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
		// 自定义文案优先
		if (isNotBlank(opts.deleteConfirmMessage)) {
			return opts.deleteConfirmMessage
		}

		const count = idList.length
		const names: string[] = opts.deleteNameKey ? (rows.map(row => (row as any)[opts.deleteNameKey!] as string).filter(isNotBlank) as string[]) : []

		if (isEmpty(names)) {
			return count === 1 ? `确认要删除${subject}吗？` : `确认要删除这${count}条${subject}吗？`
		} else if (names.length <= 3) {
			return `确认要删除${subject}【${names.join('、')}】吗？`
		} else {
			return `确认要删除【${names.slice(0, 3).join('、')}…】等${count}个${subject}吗？`
		}
	}

	const deleteExecute = (arg?: Key | VO) => {
		if (!opts.deleteApi) {
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
				opts.deleteApi!(idList)
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

	// --- 导出 ---
	const exportExecute = (id?: Key) => {
		if (!opts.exportApi) {
			ElMessage.warning('未配置导出接口')
			return
		}

		const idList = (id ? [id] : [...selectedIds.value]) as KeyArray
		if (isEmpty(idList)) {
			ElMessage.warning(`请选择要导出的${subject}`)
			return
		}

		exportLoading.value = true
		opts.exportApi!(idList)
			.then(() => {
				ElMessage.success(opts.exportSuccessMessage)
				clearSelection()
			})
			.finally(() => {
				exportLoading.value = false
			})
	}

	// --- 导入 ---
	const importExecute = (file: File, params: Record<string, any> = {}) => {
		if (!file) {
			ElMessage.warning('请选择要导入的文件')
			return
		}
		if (!opts.importApi) {
			ElMessage.warning('未配置导入接口')
			return
		}

		const formData = new FormData()
		formData.append('file', file)
		Object.keys(params).forEach(key => formData.append(key, params[key]))

		opts.importApi!(formData).then(() => {
			ElMessage.success(opts.importSuccessMessage)
			getDataList()
		})
	}

	// ====== 4. 列配置管理 ======
	const settingsStore = useTableSettingsStore()
	const defaultColumns = opts.tableColumns!

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
		opts.tableKey
			? settingsStore.getColumns(opts.tableKey)
				? mergeWithDefaults(settingsStore.getColumns(opts.tableKey)!)
				: cloneColumns(defaultColumns)
			: cloneColumns(defaultColumns)
	)

	const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

	const persistColumns = () => {
		if (opts.tableKey) {
			settingsStore.saveColumns(opts.tableKey, toRaw(columns.value) as any)
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
		// 搜索区
		search: reactive({
			fields: searchFields,
			form: queryForm,
			visible: queryShow
		}),
		// 查询操作
		query: reactive({
			getDataList,
			reset: resetQueryHandle,
			onPageChange,
			onSizeChange,
			onSortChange
		}),
		// 表格数据
		table: reactive({
			data: dataList,
			loading,
			pagination: computed<Pagination>(() => ({ current: pageNum.value, size: pageSize.value, total: total.value })),
			index: tableIndex
		}),
		// 多选
		selection: reactive({
			rows: selectedRows,
			ids: selectedIds,
			onChange: onSelectionChange,
			clear: clearSelection
		}),
		// 操作（删除 + 导出/导入）
		action: reactive({
			delete: {
				execute: deleteExecute,
				loading: deleteLoading
			},
			export: {
				execute: exportExecute,
				loading: exportLoading
			},
			import: {
				execute: importExecute
			}
		}),
		// 列配置
		columnConfig: reactive({
			list: columns,
			visible: visibleColumns,
			reset: resetColumnSettings
		}),
		// UI 引用
		refs: reactive({
			cardMaximized,
			primaryKey
		})
	}
}
