import type { UseCrudTableOptions, UseCrudTableReturn } from '@/types/hooks/use-crud-table'
import type { Pagination, SortChangePayload, TableColumnSchema } from '@/types/schema'
import { normalizeSearchField } from '@/types/schema'
import type { Key, KeyArray } from '@/types/common'
import type { PageVO } from '@/types/api/common'
import { useTableSettingsStore } from '@/store/table-settings-store'
import { defaultsDeep, isEmpty, isNotBlank } from '@/utils/tool'
import { toValue } from 'vue'

// ======== 表格列默认值 ========
export const defaultTableColumnOptions = (): Partial<TableColumnSchema> => ({
	align: 'center',
	showOverflowTooltip: true
})

// ======== 内置固定列工厂函数 ========
export const createBuiltinSelection = (): TableColumnSchema => ({
	key: 'selection',
	type: 'selection',
	label: '',
	width: 50,
	align: 'center',
	fixed: 'left',
	disabled: true,
	reserveSelection: true
})
export const createBuiltinIndex = (): TableColumnSchema => ({
	key: 'index',
	type: 'index',
	label: '序号',
	width: 60,
	align: 'center',
	disabled: true
})
export const createBuiltinCreateTime = (): TableColumnSchema => ({
	key: 'createTime',
	label: '创建时间',
	minWidth: 120,
	sortable: 'custom',
	align: 'center',
	showOverflowTooltip: true
})
export const createBuiltinUpdateTime = (): TableColumnSchema => ({
	key: 'updateTime',
	label: '修改时间',
	minWidth: 120,
	sortable: 'custom',
	align: 'center',
	showOverflowTooltip: true
})
export const createBuiltinOperation = (): TableColumnSchema => ({
	key: 'operation',
	label: '操作',
	fixed: 'right',
	width: 150,
	align: 'center',
	disabled: true
})

export const useCrudTable = <VO = any, Query = any>(opts: UseCrudTableOptions<VO, Query>): UseCrudTableReturn<VO, Query> => {
	// ====== 默认值（仅填充缺失属性，保留 reactive 响应性） ======
	const defaultOptions = (): Partial<UseCrudTableOptions<VO, Query>> => ({
		primaryKey: 'id',
		subject: '数据',
		isPage: true,
		pageSizes: [10, 20, 50, 100, 200],
		pageSize: 10,
		mountedGetData: true,
		resetQueryGetData: true,
		showToolbarLeft: true,
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
	// 不能写成 reactive({ ...(opts.initQueryForm!() as object) })：展开一个未实例化的泛型形参
	// 会把类型擦成 {}，导致页面访问 tableHook.search.form.xxx 报 TS2339。
	// reactive 的形参要求 object，而裸泛型形参的隐式约束是 unknown，所以需先断言为 object。
	const queryForm = reactive((opts.initQueryForm?.() ?? {}) as object) as Query
	const dataList = ref([] as VO[])
	const loading = ref(false)
	const pageNum = ref(1)
	const pageSize = ref(opts.pageSize!)
	const total = ref(0)
	const order = ref('')
	const asc = ref(false)
	// el-table 组件实例，由 QueryTable 挂载后通过 selection.bindTable() 注入。
	// 必须用 shallowRef：组件实例内含 DOM 与内部 store，被 reactive 深代理会引发性能问题。
	// 声明在这一节（而非多选小节）是因为 resetQueryHandle 需要调它的 clearSort()。
	const tableInstance = shallowRef<any>()

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
				// delete 必须放在判断内：值不是二元数组时保留原始字段，
				// 否则查询条件会静默消失（既不拆出起止字段，也不传原值）
				if (Array.isArray(val) && val.length === 2) {
					const [startKey, endKey] = field.rangeFields ?? [`${field.prop}Start`, `${field.prop}End`]
					result[startKey] = val[0]
					result[endKey] = val[1]
					delete result[field.prop]
				}
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

	// 把查询表单恢复到 initQueryForm() 的初始值
	const resetQueryForm = () => {
		const initial = (opts.initQueryForm?.() ?? {}) as Record<string, any>
		const form = queryForm as Record<string, any>
		// 先删多余 key：el-form 的 resetFields() 走 setProp(model, prop, initialValue)，
		// prop 不在 model 上时会新建 key，这些 key 会随 buildQueryForm() 的展开一起发给后端。
		// 套用与表单侧 assignFormData 相同的"先删后 assign"，不套 Object.assign 的"只增不减"语义。
		for (const key of Object.keys(form)) {
			if (!(key in initial)) {
				delete form[key]
			}
		}
		Object.assign(form, initial)
	}

	// 重置查询
	const resetQueryHandle = () => {
		// 清查询条件必须在 hook 里做，不能只依赖 SearchForm 的 resetFields()：
		// 1) 多个页面（枚举项抽屉、项目页内嵌表格等）在 init() 里直接调 query.reset()，根本不经过 SearchForm；
		// 2) SearchForm 根节点是 v-if，折叠后重新展开时 el-form 会重新记录初始值，
		//    此刻 queryForm 里若残留着上次的条件，就会被当成"初始值"，resetFields 只会重置回残留值。
		resetQueryForm()
		// 排序属于查询条件：不清掉的话，点「重置」后表单虽然回到初始值，
		// 但请求仍带 orderItemList，用户无法回到「无排序」状态。
		order.value = ''
		asc.value = false
		// 表头箭头由 el-table 内部 states 驱动，与 order/asc 无关。clearSort 内部传了 silent，
		// 只重置内部排序状态、不会 emit sort-change，因此不会多打一次请求。
		tableInstance.value?.clearSort()
		// 不用 nextTick 包 getDataList：buildQueryForm 同步读的就是上面刚改完的 queryForm / order / asc，
		// clearSort 也是同步方法，没有任何一步依赖 DOM 更新。
		if (resetQueryGetData) {
			getDataList()
		}
	}

	// 分页：页码变化
	const onPageChange = (page: number) => {
		// 改每页条数时 el-pagination 会先同步 emit size-change（onSizeChange 已把页码重置为 1），
		// 随后 watch 到总页数变少，再 emit 一次 current-change(1)。此时页码其实没变，
		// 不拦掉就会用同样的参数再查一次。
		if (page === pageNum.value) return
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
	const onSortChange = ({ order: sortOrder, prop }: SortChangePayload) => {
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
	const bindTable = (instance: any) => {
		tableInstance.value = instance
	}

	const onSelectionChange = (selections: VO[]) => {
		selectedRows.value = selections
		selectedIds.value = selections.map(item => (item as any)[primaryKey]) as KeyArray
	}

	const clearSelection = () => {
		// 必须先清 el-table 内部的 selection store：reserveSelection 模式下它不会自行剔除已不在数据里的行，
		// 只清本地镜像会导致下次勾选时旧行回流进 selectedIds（如批量删除后再次删除会带上已删除的 id）。
		// el-table.clearSelection() 会 emit selection-change([])，本地镜像随之同步，下面两行为幂等兜底。
		tableInstance.value?.clearSelection()
		selectedIds.value = []
		selectedRows.value = []
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
	// 返回 Promise 而非 void：el-upload 的 http-request 契约是 UploadRequestHandler，
	// 要求返回 XMLHttpRequest | Promise<unknown>；且 el-upload 靠这个 Promise 判定上传何时结束，
	// 返回 void 会让它跳过状态处理，接口失败时还会变成无人捕获的 floating rejection。
	const importExecute = (file: File, params: Record<string, any> = {}): Promise<void> => {
		if (!file) {
			ElMessage.warning('请选择要导入的文件')
			return Promise.resolve()
		}
		if (!opts.importApi) {
			ElMessage.warning('未配置导入接口')
			return Promise.resolve()
		}

		const formData = new FormData()
		formData.append('file', file)
		Object.keys(params).forEach(key => formData.append(key, params[key]))

		return opts.importApi!(formData).then(() => {
			ElMessage.success(opts.importSuccessMessage)
			getDataList()
		})
	}

	// ====== 4. 列配置管理 ======
	const settingsStore = useTableSettingsStore()
	const defaultColumns = opts.tableColumns!

	// 递归深拷贝（children 一并克隆；不用 structuredClone，因 formatter 是函数无法克隆）
	const cloneColumns = (cols: TableColumnSchema[]): TableColumnSchema[] =>
		cols.map(col => (col.children?.length ? { ...col, children: cloneColumns(col.children) } : { ...col }))

	// 递归合并：按 key 逐层匹配，保留用户改动的 visible/fixed/label，并递归合并分组的 children；
	// 代码里新增（saved 中缺失）的默认列按默认顺序补回。
	const mergeWithDefaults = (saved: TableColumnSchema[], defaults: TableColumnSchema[]): TableColumnSchema[] => {
		const defaultMap = new Map(defaults.map(col => [col.key, col]))
		const savedKeys = new Set(saved.map(col => col.key))

		const merged = saved.map(savedCol => {
			const defaultCol = defaultMap.get(savedCol.key)
			if (!defaultCol) return cloneColumns([savedCol])[0]
			const result: TableColumnSchema = {
				...defaultCol,
				visible: savedCol.visible,
				fixed: savedCol.fixed,
				label: savedCol.label ?? defaultCol.label
			}
			// 分组列：以默认分组结构为基底递归合并 children
			if (defaultCol.children?.length || savedCol.children?.length) {
				result.children = mergeWithDefaults(savedCol.children ?? [], defaultCol.children ?? [])
			}
			return result
		})

		for (const col of defaults) {
			if (!savedKeys.has(col.key)) {
				merged.push(cloneColumns([col])[0])
			}
		}

		return merged
	}

	const savedColumns = opts.tableKey ? (settingsStore.getColumns(opts.tableKey) as TableColumnSchema[] | undefined) : undefined
	const columns = ref<TableColumnSchema[]>(savedColumns ? mergeWithDefaults(savedColumns, defaultColumns) : cloneColumns(defaultColumns))

	const persistColumns = () => {
		if (opts.tableKey) {
			settingsStore.saveColumns(opts.tableKey, toRaw(columns.value) as any)
		}
	}

	const resetColumnSettings = () => {
		columns.value = cloneColumns(defaultColumns)
		persistColumns()
	}

	// 列设置变更（显隐/固定/拖拽/改名）实时持久化
	watch(columns, persistColumns, { deep: true })

	// ====== 5. UI 状态 ======
	const queryShow = ref(true)
	const cardMaximized = ref(false)

	const tableIndex = (index: number) => {
		return (pageNum.value - 1) * pageSize.value + index + 1
	}

	// ====== 6. 生命周期 ======
	// 不用 nextTick 包一层：Vue 的挂载顺序是子先父后，页面 onMounted 触发时
	// QueryTable 的 onMounted 已经调过 bindTable，tableInstance 已就位。
	onMounted(() => {
		if (mountedGetData) {
			resetQueryHandle()
		}
	})

	// ====== 返回值：7 组 reactive 包裹 ======
	// search / table / selection 三组需要按契约收口：reactive 的 UnwrapNestedRefs 会把裸泛型形参
	// 包成 UnwrapRef<Query> / UnwrapRefSimple<VO>，而泛型未实例化时这些条件类型无法化简，
	// 与契约里的 Query / VO 在类型层面互不相认（运行时是同一个对象，不存在实际差异）。
	// 其余四组无泛型参与，保持原样以获得完整的实现/契约漂移检查。
	return {
		// 搜索区
		search: reactive({
			fields: searchFields,
			form: queryForm,
			visible: queryShow
		}) as UseCrudTableReturn<VO, Query>['search'],
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
			// 不分页时返回 undefined，DataTable 的 v-if="pagination" 据此隐藏分页条
			pagination: computed<Pagination | undefined>(() =>
				isPage ? { current: pageNum.value, size: pageSize.value, total: total.value } : undefined
			),
			index: tableIndex
		}) as UseCrudTableReturn<VO, Query>['table'],
		// 多选
		selection: reactive({
			rows: selectedRows,
			ids: selectedIds,
			onChange: onSelectionChange,
			clear: clearSelection,
			bindTable
		}) as UseCrudTableReturn<VO, Query>['selection'],
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
			reset: resetColumnSettings
		}),
		// UI 引用
		refs: reactive({
			cardMaximized,
			primaryKey,
			showToolbarLeft: opts.showToolbarLeft!
		})
	}
}
