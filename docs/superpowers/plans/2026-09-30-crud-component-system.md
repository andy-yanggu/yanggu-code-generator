# CRUD 组件系统重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 useTableV2 + config-table + search-form 分散组合模式封装为四层配置驱动组件树，页面只需提供 schema 配置和 API 引用。

**Architecture:** CrudPage 内置 useCrudTable + useCrudForm 两个 hook，编排 QueryTable（SearchForm + DataTable）和 CrudDialog（FormField → DynamicField）。DataTable 全新实现，内置工具栏和列设置面板。DynamicField 通过显式 v-if 分支渲染 Element Plus 表单控件。

**Tech Stack:** Vue 3.5+ `<script setup>`、Element Plus 2.11.3、TypeScript 5.9.3、sortablejs、pinia-plugin-persistedstate

**Spec:** `docs/superpowers/specs/2026-09-30-crud-component-system-design.md`

## Global Constraints

- Tab 缩进、单引号、无分号、中文注释、printWidth 150
- auto-import 仅注入值不注入 Vue 类型 → `Ref`/`ComputedRef` 等类型须显式 `import type`
- defineModel 声明的变量不出现在 props 上 → 脚本内用 `xxx.value` 访问
- hook 分组返回值必须用 `reactive()` 包裹（不能用 computed），确保模板嵌套 ref 解包
- composable 类型契约放 `src/types/hooks/use-xxx.ts`，实现放 `src/hooks/use-xxx.ts`
- types/hooks/*.ts 内部禁止 `from '@/types'`，必须走具体子路径 `@/types/common`、`@/types/api/common`
- `<component :is>` 必须传组件对象，不能传字符串
- select/radio/checkbox 用显式 v-if 模板标签写子组件，不依赖 options prop
- 列配置持久化恢复：以 defaultColumns 为基底 mergeWithDefaults，禁止 structuredClone
- 校验职责随组件下沉 → hook 侧 formRef 做空值守卫
- vue-tsc 基线 22 个既有错误，新增文件零新增错误

## File Structure

| 文件 | 职责 |
|------|------|
| `src/types/schema/index.ts` | Schema 类型定义 |
| `src/types/hooks/use-crud-table.ts` | useCrudTable 类型契约 |
| `src/types/hooks/use-crud-form.ts` | useCrudForm 类型契约 |
| `src/types/hooks/index.ts` | 追加导出（modify） |
| `src/types/index.ts` | 追加 schema 导出（modify） |
| `src/hooks/use-crud-table.ts` | 表格 hook |
| `src/hooks/use-crud-form.ts` | 表单 hook |
| `src/hooks/index.ts` | 追加导出（modify） |
| `src/components/crud/internal/DynamicField.vue` | 动态字段渲染 |
| `src/components/crud/internal/FormField.vue` | 表单字段包装 |
| `src/components/crud/SearchForm.vue` | 搜索表单 |
| `src/components/crud/DataTable.vue` | 表格 + 工具栏 + 列设置 |
| `src/components/crud/QueryTable.vue` | 搜索 + 表格聚合 |
| `src/components/crud/CrudDialog.vue` | 弹窗表单 |
| `src/components/crud/CrudPage.vue` | 顶层页面组件 |
| `src/views/gen/field-type/test.vue` | 测试页面 |

---

### Task 1: Schema 类型定义 + 导出更新

**Files:**
- Create: `src/types/schema/index.ts`
- Modify: `src/types/index.ts`

**Interfaces:**
- Produces: `ComponentType`, `SearchFieldSchema`, `FormFieldSchema`, `TableColumnSchema`, `FormMode`, `Pagination`

- [ ] **Step 1: 创建 `src/types/schema/index.ts`**

```ts
import type { Key, PageQuery } from '@/types/common'
import type { PageVO } from '@/types/api/common'

// 组件类型（无 el- 前缀，渲染层通过 COMPONENT_MAP 映射）
export type ComponentType =
	| 'input'
	| 'select'
	| 'textarea'
	| 'radio'
	| 'checkbox'
	| 'switch'
	| 'date-picker'
	| 'input-number'
	| 'tree-select'
	| 'cascader'

// 表单模式
export type FormMode = 'add' | 'update' | 'detail'

// 分页参数
export interface Pagination {
	current: number
	size: number
	total: number
}

// 搜索字段 schema
export interface SearchFieldSchema {
	prop: string
	label: string
	component?: ComponentType
	placeholder?: string
	width?: number | string
	options?: { label: string; value: any }[]
	filterable?: boolean
	rangeFields?: [string, string]
}

// 表单字段 schema
export interface FormFieldSchema {
	prop: string
	label: string
	component?: ComponentType
	placeholder?: string
	options?: { label: string; value: any }[]
	filterable?: boolean
	span?: number
	detailFormatter?: (value: any) => string
}

// 表格列 schema
export interface TableColumnSchema {
	key: string
	label: string
	prop?: string
	type?: 'selection' | 'index'
	width?: number | string
	minWidth?: number
	fixed?: 'left' | 'right' | boolean
	align?: 'left' | 'center' | 'right'
	headerAlign?: 'left' | 'center' | 'right'
	sortable?: boolean | 'custom'
	formatter?: (row: any, column?: any, cellValue?: any) => any
	visible?: boolean
	disabled?: boolean
	showOverflowTooltip?: boolean
}

// 归一化搜索字段（补全默认值）
export const normalizeSearchField = (field: SearchFieldSchema): SearchFieldSchema & { component: ComponentType; placeholder: string; width: number | string } => {
	const component = field.component ?? 'input'
	const placeholder = field.placeholder ?? (component === 'select' ? `请选择${field.label}` : `请输入${field.label}`)
	return {
		...field,
		component,
		placeholder,
		width: field.width ?? 160
	}
}
```

- [ ] **Step 2: 修改 `src/types/index.ts`，追加 schema 导出**

在文件末尾追加：

```ts
// Schema 类型
export * from '@/types/schema'
```

- [ ] **Step 3: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "schema"`
Expected: 无 schema 相关新增错误

- [ ] **Step 4: Commit**

```bash
git add src/types/schema/index.ts src/types/index.ts
git commit -m "feat(types): add schema type definitions for CRUD component system"
```

---

### Task 2: Hook 类型契约

**Files:**
- Create: `src/types/hooks/use-crud-table.ts`
- Create: `src/types/hooks/use-crud-form.ts`
- Modify: `src/types/hooks/index.ts`

**Interfaces:**
- Consumes: `TableColumnSchema`, `SearchFieldSchema`, `Pagination` from `@/types/schema`
- Consumes: `Key`, `PageVO` from `@/types/common`, `@/types/api/common`
- Produces: `UseCrudTableOptions`, `UseCrudFormOptions`

- [ ] **Step 1: 创建 `src/types/hooks/use-crud-table.ts`**

```ts
import type { Key, PageQuery } from '@/types/common'
import type { PageVO } from '@/types/api/common'
import type { TableColumnSchema, SearchFieldSchema } from '@/types/schema'

// 数据列表接口
type DataListApi<VO> = (query: any) => Promise<PageVO<VO>>
// 批量删除接口
type DeleteListApi = (idList: Key[]) => Promise<void>

/**
 * useCrudTable 配置选项
 */
export interface UseCrudTableOptions<VO = any> {
	// ---- 必填 ----
	/** 分页查询接口 */
	dataListApi: DataListApi<VO>
	/** 查询表单初始值工厂 */
	initQueryForm: () => any
	/** 表格列配置 */
	columns: TableColumnSchema[]

	// ---- 搜索 ----
	/** 搜索字段配置 */
	searchSchema?: SearchFieldSchema[]

	// ---- 分页 ----
	/** 每页条数，默认 10 */
	pageSize?: number
	/** 每页条数选项 */
	pageSizes?: number[]

	// ---- 删除 ----
	/** 批量删除接口 */
	deleteApi?: DeleteListApi
	/** 主键字段名，默认 'id' */
	primaryKey?: string
	/** 主体名称，默认 '数据' */
	subject?: string
	/** 删除确认时展示的名称字段 */
	deleteNameKey?: string

	// ---- 列设置 ----
	/** 持久化 key */
	tableKey?: string

	// ---- 行为 ----
	/** 是否在 mounted 时自动加载，默认 true */
	mountedGetData?: boolean
	/** 重置后是否自动查询，默认 true */
	resetQueryGetData?: boolean
	/** 固定查询上下文（不被 reset 覆盖） */
	queryContext?: Record<string, any>
}
```

- [ ] **Step 2: 创建 `src/types/hooks/use-crud-form.ts`**

```ts
import type { Key } from '@/types/common'
import type { FormItemRule } from 'element-plus'
import type { FormMode } from '@/types/schema'

/**
 * useCrudForm 配置选项
 */
export interface UseCrudFormOptions {
	/** 提交接口 */
	submitApi: (data: any) => Promise<any>
	/** 详情接口 */
	detailApi?: (id: Key) => Promise<any>
	/** 表单数据初始值工厂 */
	initFormData: () => any
	/** 弹窗标题主体 */
	subject?: string
	/** 表单校验规则 */
	rules?: Record<string, FormItemRule[]>
	/** 表单标签宽度，默认 '100px' */
	labelWidth?: string
	/** 提交成功回调（默认关闭弹窗 + 刷新表格） */
	submitAfter?: (data: any) => void
}
```

- [ ] **Step 3: 修改 `src/types/hooks/index.ts`，追加导出**

在文件末尾追加：

```ts
export * from '@/types/hooks/use-crud-table'
export * from '@/types/hooks/use-crud-form'
```

- [ ] **Step 4: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "use-crud"`
Expected: 无新增错误

- [ ] **Step 5: Commit**

```bash
git add src/types/hooks/use-crud-table.ts src/types/hooks/use-crud-form.ts src/types/hooks/index.ts
git commit -m "feat(types): add useCrudTable and useCrudForm type contracts"
```

---

### Task 3: useCrudTable Hook

**Files:**
- Create: `src/hooks/use-crud-table.ts`
- Modify: `src/hooks/index.ts`

**Interfaces:**
- Consumes: `UseCrudTableOptions` from `@/types/hooks/use-crud-table`
- Consumes: `TableColumnSchema`, `SearchFieldSchema`, `Pagination` from `@/types/schema`
- Consumes: `normalizeSearchField` from `@/types/schema`
- Consumes: `useTableSettingsStore` from `@/store/table-settings-store`
- Produces: 7 组 reactive 返回值

- [ ] **Step 1: 创建 `src/hooks/use-crud-table.ts`**

```ts
import type { UseCrudTableOptions } from '@/types/hooks/use-crud-table'
import type { TableColumnSchema, SearchFieldSchema, Pagination } from '@/types/schema'
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

		ElMessageBox.confirm(buildDeleteConfirmMessage(rows, idList), '提示', {
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
```

- [ ] **Step 2: 修改 `src/hooks/index.ts`，追加导出**

在文件末尾追加：

```ts
export * from '@/hooks/use-crud-table'
export * from '@/hooks/use-crud-form'
```

- [ ] **Step 3: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "use-crud-table"`
Expected: 无新增错误

- [ ] **Step 4: Commit**

```bash
git add src/hooks/use-crud-table.ts src/hooks/index.ts
git commit -m "feat(hooks): add useCrudTable composable with grouped return values"
```

---

### Task 4: useCrudForm Hook

**Files:**
- Create: `src/hooks/use-crud-form.ts`

**Interfaces:**
- Consumes: `UseCrudFormOptions` from `@/types/hooks/use-crud-form`
- Consumes: `FormMode` from `@/types/schema`
- Produces: 3 组 reactive 返回值（dialog / form / submit）

- [ ] **Step 1: 创建 `src/hooks/use-crud-form.ts`**

```ts
import type { UseCrudFormOptions } from '@/types/hooks/use-crud-form'
import type { Key } from '@/types/common'
import type { FormMode } from '@/types/schema'

export const useCrudForm = (options: UseCrudFormOptions) => {
	const subject = options.subject ?? '数据'
	const labelWidth = options.labelWidth ?? '100px'

	// ====== 弹窗状态 ======
	const visible = ref(false)
	const mode = ref<FormMode>('add')
	const currentId = ref<Key>(null)

	// ====== 表单状态 ======
	const formData = reactive({ ...options.initFormData() })
	const formRef = ref()

	// ====== 提交状态 ======
	const submitLoading = ref(false)

	// 标题映射
	const titleMap: Record<string, string> = {
		add: '新增',
		update: '修改',
		detail: ''
	}

	const dialogTitle = computed(() => {
		if (mode.value === 'detail') return `${subject}详情`
		const action = titleMap[mode.value] ?? '操作'
		return `${action}${subject}`
	})

	// 打开弹窗
	const open = (openMode: FormMode, id?: Key) => {
		mode.value = openMode
		currentId.value = id ?? null
		visible.value = true

		nextTick(() => {
			// 重置表单数据
			Object.assign(formData, options.initFormData())
			formRef.value?.clearValidate()

			if (id && openMode !== 'add') {
				// 调用详情接口
				options.detailApi?.(id).then(data => {
					Object.assign(formData, data)
				})
			}
		})
	}

	// 关闭弹窗
	const close = () => {
		visible.value = false
	}

	// 提交（校验由 CrudDialog 内部处理，只有校验通过才会调用此函数）
	const submit = () => {
		if (mode.value === 'detail') {
			ElMessage.warning('详情模式无法提交')
			return
		}

		submitLoading.value = true
		options
			.submitApi(formData)
			.then(data => {
				ElMessage.success(`${subject}${titleMap[mode.value]}成功`)
				visible.value = false
				if (options.submitAfter) {
					options.submitAfter(data ?? formData)
				}
			})
			.finally(() => {
				submitLoading.value = false
			})
	}

	// ====== 返回值 ======
	return {
		dialog: reactive({
			visible,
			mode,
			title: dialogTitle,
			open,
			close
		}),
		form: reactive({
			data: formData,
			rules: options.rules ?? {},
			labelWidth,
			formRef
		}),
		submit: reactive({
			execute: submit,
			loading: submitLoading
		})
	}
}
```

- [ ] **Step 2: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "use-crud-form"`
Expected: 无新增错误

- [ ] **Step 3: Commit**

```bash
git add src/hooks/use-crud-form.ts
git commit -m "feat(hooks): add useCrudForm composable for dialog-driven form management"
```

---

### Task 5: DynamicField + FormField

**Files:**
- Create: `src/components/crud/internal/DynamicField.vue`
- Create: `src/components/crud/internal/FormField.vue`

**Interfaces:**
- DynamicField: `v-model` + `field: FormFieldSchema` → 渲染对应 Element Plus 控件
- FormField: `v-model` + `field: FormFieldSchema` + `mode: FormMode` → el-form-item 包装

- [ ] **Step 1: 创建 `src/components/crud/internal/DynamicField.vue`**

```vue
<template>
	<!-- textarea -->
	<el-input
		v-if="field.component === 'textarea'"
		v-model="model"
		type="textarea"
		:rows="3"
		:placeholder="field.placeholder"
		clearable
	/>
	<!-- input（默认） -->
	<el-input
		v-else-if="field.component === 'input' || !field.component"
		v-model="model"
		:placeholder="field.placeholder"
		clearable
	/>
	<!-- select -->
	<el-select
		v-else-if="field.component === 'select'"
		v-model="model"
		:placeholder="field.placeholder"
		:filterable="field.filterable"
		clearable
	>
		<el-option v-for="opt in field.options" :key="opt.value" :label="opt.label" :value="opt.value" />
	</el-select>
	<!-- radio -->
	<el-radio-group v-else-if="field.component === 'radio'" v-model="model">
		<el-radio v-for="opt in field.options" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
	</el-radio-group>
	<!-- checkbox -->
	<el-checkbox-group v-else-if="field.component === 'checkbox'" v-model="model">
		<el-checkbox v-for="opt in field.options" :key="opt.value" :value="opt.value" :label="opt.label" />
	</el-checkbox-group>
	<!-- switch -->
	<el-switch v-else-if="field.component === 'switch'" v-model="model" />
	<!-- date-picker -->
	<el-date-picker
		v-else-if="field.component === 'date-picker'"
		v-model="model"
		type="date"
		:placeholder="field.placeholder"
		value-format="YYYY-MM-DD"
		clearable
	/>
	<!-- input-number -->
	<el-input-number v-else-if="field.component === 'input-number'" v-model="model" />
	<!-- tree-select -->
	<el-tree-select
		v-else-if="field.component === 'tree-select'"
		v-model="model"
		:data="field.options"
		:placeholder="field.placeholder"
		check-strictly
		clearable
	/>
	<!-- cascader -->
	<el-cascader
		v-else-if="field.component === 'cascader'"
		v-model="model"
		:options="field.options"
		:placeholder="field.placeholder"
		clearable
	/>
</template>

<script setup lang="ts">
import type { FormFieldSchema } from '@/types/schema'

defineOptions({
	name: 'DynamicField'
})

const model = defineModel<any>()

defineProps<{
	field: FormFieldSchema
}>()
</script>
```

- [ ] **Step 2: 创建 `src/components/crud/internal/FormField.vue`**

```vue
<template>
	<el-form-item :label="field.label" :prop="field.prop">
		<!-- 插槽覆盖 -->
		<slot :name="`field-${field.prop}`" :model="model" :field="field">
			<!-- 详情模式：纯文本展示 -->
			<span v-if="mode === 'detail'" class="detail-text">
				{{ field.detailFormatter ? field.detailFormatter(model[field.prop]) : model[field.prop] }}
			</span>
			<!-- 编辑模式：动态字段 -->
			<dynamic-field v-else v-model="model[field.prop]" :field="field" />
		</slot>
	</el-form-item>
</template>

<script setup lang="ts">
import type { FormFieldSchema, FormMode } from '@/types/schema'
import DynamicField from './DynamicField.vue'

defineOptions({
	name: 'FormField'
})

const model = defineModel<Record<string, any>>({ required: true })

defineProps<{
	field: FormFieldSchema
	mode: FormMode
}>()
</script>

<style scoped>
.detail-text {
	color: var(--el-text-color-regular);
	line-height: 32px;
}
</style>
```

- [ ] **Step 3: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "DynamicField|FormField"`
Expected: 无新增错误

- [ ] **Step 4: Commit**

```bash
git add src/components/crud/internal/DynamicField.vue src/components/crud/internal/FormField.vue
git commit -m "feat(crud): add DynamicField and FormField internal components"
```

---

### Task 6: SearchForm

**Files:**
- Create: `src/components/crud/SearchForm.vue`

**Interfaces:**
- Consumes: `SearchFieldSchema` from `@/types/schema`
- `v-model` → queryForm, `fields` → 搜索字段配置, `visible` → 显隐, `loading` → 查询加载态
- 校验职责下沉到组件内部（emit search 前先 validate）

- [ ] **Step 1: 创建 `src/components/crud/SearchForm.vue`**

```vue
<template>
	<el-card v-if="visible" class="layout-query-card" shadow="hover">
		<el-form ref="formRef" :inline="true" :model="model" @keyup.enter="handleSearch">
			<el-form-item v-for="field in normalizedFields" :key="field.prop" :label="field.label" :prop="field.prop">
				<!-- 插槽覆盖 -->
				<slot :name="field.prop" :field="field" :form="model">
					<dynamic-field v-model="model[field.prop]" :field="field" />
				</slot>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button :icon="Refresh" @click="handleReset">重置</el-button>
			</el-form-item>
		</el-form>
	</el-card>
</template>

<script setup lang="ts">
import { Search, Refresh } from '@element-plus/icons-vue'
import type { SearchFieldSchema } from '@/types/schema'
import { normalizeSearchField } from '@/types/schema'
import DynamicField from './internal/DynamicField.vue'

defineOptions({
	name: 'CrudSearchForm'
})

const model = defineModel<Record<string, any>>({ required: true })

const props = defineProps<{
	fields: SearchFieldSchema[]
	visible: boolean
	loading?: boolean
}>()

const emit = defineEmits<{
	(e: 'search'): void
	(e: 'reset'): void
}>()

const formRef = ref()

// 归一化搜索字段
const normalizedFields = computed(() => props.fields.map(normalizeSearchField))

// 查询（内部校验）
const handleSearch = async () => {
	try {
		await formRef.value?.validate()
		emit('search')
	} catch {
		// 校验不通过
	}
}

// 重置
const handleReset = () => {
	formRef.value?.resetFields()
	emit('reset')
}
</script>
```

- [ ] **Step 2: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "SearchForm"`
Expected: 无新增错误

- [ ] **Step 3: Commit**

```bash
git add src/components/crud/SearchForm.vue
git commit -m "feat(crud): add SearchForm component with dynamic field rendering"
```

---

### Task 7: DataTable（全新表格 + 工具栏 + 列设置）

**Files:**
- Create: `src/components/crud/DataTable.vue`

**Interfaces:**
- Consumes: `TableColumnSchema`, `Pagination` from `@/types/schema`
- `v-model:columns` → 列配置双向绑定
- `v-model:showSearch` → 搜索显隐
- `v-model:queryLoading` → 查询加载态
- `v-model:maximized` → 最大化
- 插槽：`#toolbar-left`、`#{column.key}`（透传表格列插槽）、`#operation`（操作列）
- Events: `@selection-change`、`@sort-change`、`@page-change`、`@size-change`、`@get-data-list`、`@reset-columns`

这是最大的组件，包含工具栏 + 列设置面板 + 表格 + 分页。

- [ ] **Step 1: 创建 `src/components/crud/DataTable.vue`**

```vue
<template>
	<el-card ref="cardRef" class="layout-table-card" :class="{ 'is-maximized': maximized }" shadow="hover">
		<!-- 工具栏 -->
		<template #header>
			<div class="table-tool-bar">
				<div class="table-tool-bar-left">
					<slot name="toolbar-left" />
				</div>
				<div class="table-tool-bar-right">
					<el-space size="small" :spacer="spacer">
						<!-- 搜索 -->
						<icon-button
							size="16px"
							:el-icon="Search"
							:tooltip="showSearch ? '隐藏搜索' : '显示搜索'"
							@click="toggleSearch()"
						/>
						<!-- 刷新 -->
						<icon-button
							size="16px"
							:el-icon="Refresh"
							tooltip="刷新表格"
							:loading="queryLoading"
							:loading-icon="Refresh"
							@click="emit('getDataList')"
						/>
						<!-- 列设置 -->
						<el-popover
							placement="top"
							:width="300"
							trigger="click"
							popper-class="yanggu-column-settings-popover"
							:visible="columnSettingsVisible"
							@show="handlePopoverShow"
							@hide="handlePopoverHide"
						>
							<template #reference>
								<span class="column-settings-trigger">
									<icon-button size="16px" :el-icon="Setting" tooltip="列设置" @click="columnSettingsVisible = !columnSettingsVisible" />
								</span>
							</template>
							<div class="column-settings-panel">
								<div class="column-settings-header">
									<span class="column-settings-title">列设置</span>
									<el-button link type="primary" size="small" @click="resetColumns">重置</el-button>
								</div>
								<div ref="columnListRef" class="column-list">
									<div
										v-for="col in columns"
										:key="col.key"
										class="column-list-item"
										:class="{ 'is-fixed': col.fixed || col.disabled, 'is-disabled': col.disabled, 'is-editing': editingKey === col.key }"
									>
										<!-- 编辑模式 -->
										<template v-if="editingKey === col.key">
											<span class="drag-handle"><el-icon :size="14"><Lock /></el-icon></span>
											<el-input
												v-model="editingTitle"
												size="small"
												placeholder="请输入列名"
												class="rename-input"
												@click.stop
												@keydown.enter="saveRename()"
												@keydown.escape="cancelRename()"
											/>
											<div class="rename-btns">
												<el-button type="primary" :icon="Check" link size="small" @click.stop="saveRename()" />
												<el-button :icon="Close" link size="small" @click.stop="cancelRename()" />
											</div>
										</template>
										<!-- 显示模式 -->
										<template v-else>
											<span class="drag-handle">
												<el-icon v-if="col.fixed || col.disabled" :size="14"><Lock /></el-icon>
												<el-icon v-else :size="14"><Rank /></el-icon>
											</span>
											<el-checkbox
												:model-value="col.visible !== false"
												:disabled="col.disabled"
												@change="(val: boolean | string | number) => toggleColumnVisible(col, val)"
											>
												{{ col.label }}
											</el-checkbox>
											<template v-if="!col.disabled">
												<button class="pin-btn" :class="{ 'is-active': col.fixed === 'left' }" title="固定在左边" @click="toggleFixed(col, 'left')">
													<el-icon :size="14"><ArrowLeft /></el-icon>
												</button>
												<button class="pin-btn" :class="{ 'is-active': col.fixed === 'right' }" title="固定在右边" @click="toggleFixed(col, 'right')">
													<el-icon :size="14"><ArrowRight /></el-icon>
												</button>
											</template>
											<el-button
												v-if="!col.disabled"
												:icon="Edit"
												type="primary"
												link
												size="small"
												class="rename-btn"
												title="重命名"
												@click.stop="startRename(col)"
											/>
										</template>
									</div>
								</div>
							</div>
						</el-popover>
						<!-- 最大化 -->
						<icon-button
							size="16px"
							:el-icon="FullScreen"
							:tooltip="maximized ? '退出全屏' : '表格全屏'"
							@click="handleToggleMaximized()"
						/>
					</el-space>
				</div>
			</div>
		</template>

		<!-- 表格 -->
		<el-table
			ref="tableRef"
			v-loading="loading"
			v-bind="$attrs"
			:data="data"
			:max-height="maxHeight"
			border
			row-key="id"
			@selection-change="(s: any[]) => emit('selectionChange', s)"
			@sort-change="(s: any) => emit('sortChange', s)"
		>
			<el-table-column v-for="col in visibleColumns" :key="col.key" v-bind="toColumnProps(col)">
				<!-- 操作列：使用 operation 插槽 -->
				<template v-if="col.key === 'operation'" #default="scope">
					<slot name="operation" v-bind="scope" />
				</template>
				<!-- 其他列：透传命名插槽 -->
				<template v-else-if="$slots[col.key]" #default="scope">
					<slot :name="col.key" v-bind="scope" />
				</template>
			</el-table-column>
			<template #empty>
				<slot name="empty">
					<el-empty description="暂无数据" :image-size="120" />
				</slot>
			</template>
		</el-table>

		<!-- 分页 -->
		<el-pagination
			v-if="pagination"
			:current-page="pagination.current"
			:page-sizes="pageSizes"
			:page-size="pagination.size"
			:total="pagination.total"
			background
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="(s: number) => emit('sizeChange', s)"
			@current-change="(p: number) => emit('pageChange', p)"
		/>
	</el-card>
</template>

<script setup lang="ts">
import {
	ArrowLeft, ArrowRight, Check, Close, Edit, FullScreen, Lock, Rank, Refresh, Search, Setting
} from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { ElDivider } from 'element-plus'
import IconButton from '@/components/icon-button/index.vue'
import type { TableColumnSchema, Pagination } from '@/types/schema'

defineOptions({
	name: 'CrudDataTable',
	inheritAttrs: false
})

// ====== defineModel ======
const showSearch = defineModel('showSearch', { type: Boolean, default: true })
const queryLoading = defineModel('queryLoading', { type: Boolean, default: false })
const maximized = defineModel('maximized', { type: Boolean, default: false })
const columns = defineModel<TableColumnSchema[]>('columns', { default: () => [] })

const props = withDefaults(
	defineProps<{
		data: any[]
		loading?: boolean
		pagination?: Pagination
		pageSizes?: number[]
		maxHeight?: string
	}>(),
	{
		loading: false,
		pageSizes: () => [10, 20, 50, 100, 200],
		maxHeight: '60vh'
	}
)

const emit = defineEmits<{
	(e: 'selectionChange', selections: any[]): void
	(e: 'sortChange', sort: any): void
	(e: 'pageChange', page: number): void
	(e: 'sizeChange', size: number): void
	(e: 'getDataList'): void
	(e: 'resetColumns'): void
}>()

// ====== 工具栏 ======
const spacer = h(ElDivider, { direction: 'vertical' })
const toggleSearch = useToggle(showSearch)
const toggleMaximized = useToggle(maximized)

const handleToggleMaximized = () => {
	toggleMaximized()
	if (!maximized.value) {
		ElMessage.info({ message: '按 ESC 可退出最大化', duration: 1000 })
	}
}

onKeyStroke('Escape', () => {
	if (maximized.value) maximized.value = false
})

// ====== 可见列 ======
const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

// ====== 列设置面板 ======
const columnSettingsVisible = ref(false)
const columnListRef = ref<HTMLElement>()
let sortableInstance: Sortable | null = null

// 重命名状态
const editingKey = ref<string | null>(null)
const editingTitle = ref('')

const handlePopoverShow = () => {
	nextTick(() => createSortable())
}

const handlePopoverHide = () => {
	sortableInstance?.destroy()
	sortableInstance = null
}

const createSortable = () => {
	const el = columnListRef.value
	if (!el) return
	if (sortableInstance) {
		sortableInstance.destroy()
		sortableInstance = null
	}
	sortableInstance = new Sortable(el, {
		animation: 150,
		filter: '.is-fixed',
		preventOnFilter: false,
		onMove: evt => {
			const items = Array.from(el.children)
			const draggedIdx = items.indexOf(evt.dragged)
			const targetIdx = items.indexOf(evt.related)
			const cols = columns.value
			const draggedCol = cols[draggedIdx]
			if (draggedCol?.fixed) return true
			const fixedLeftCount = cols.filter((c: TableColumnSchema) => c.fixed === 'left').length
			const fixedRightCount = cols.filter((c: TableColumnSchema) => c.fixed === 'right').length
			const total = cols.length
			if (targetIdx < fixedLeftCount) return false
			if (targetIdx >= total - fixedRightCount) return false
			return true
		},
		onEnd: evt => {
			const { oldIndex, newIndex } = evt
			if (oldIndex == null || newIndex == null || oldIndex === newIndex) return
			const arr = [...columns.value]
			const [moved] = arr.splice(oldIndex, 1)
			arr.splice(newIndex, 0, moved)
			columns.value = arr
		}
	})
}

const toggleColumnVisible = (col: TableColumnSchema, visible: boolean | string | number) => {
	const idx = columns.value.findIndex((c: TableColumnSchema) => c.key === col.key)
	if (idx === -1) return
	const updated = [...columns.value]
	updated[idx] = { ...updated[idx], visible: !!visible }
	columns.value = updated
}

const toggleFixed = (col: TableColumnSchema, direction: 'left' | 'right') => {
	const idx = columns.value.findIndex(c => c.key === col.key)
	if (idx === -1) return
	const updated = [...columns.value]
	updated[idx] = { ...updated[idx], fixed: col.fixed === direction ? false : direction }
	columns.value = updated
}

const resetColumns = () => {
	editingKey.value = null
	emit('resetColumns')
}

const startRename = (col: TableColumnSchema) => {
	editingKey.value = col.key
	editingTitle.value = col.label
}

const saveRename = () => {
	if (!editingKey.value) return
	const trimmed = editingTitle.value.trim()
	if (trimmed) {
		const idx = columns.value.findIndex(c => c.key === editingKey.value)
		if (idx !== -1) {
			const updated = [...columns.value]
			updated[idx] = { ...updated[idx], label: trimmed }
			columns.value = updated
		}
	}
	editingKey.value = null
}

const cancelRename = () => {
	editingKey.value = null
}

// ====== 列配置转换 ======
const toColumnProps = (col: TableColumnSchema): Record<string, any> => {
	const result: Record<string, any> = {}
	if (col.type) result.type = col.type
	if (col.prop) result.prop = col.prop
	if (col.label) result.label = col.label
	if (col.width) result.width = col.width
	if (col.minWidth) result.minWidth = col.minWidth
	if (col.fixed) result.fixed = col.fixed
	if (col.align) result.align = col.align
	if (col.headerAlign) result.headerAlign = col.headerAlign
	if (col.sortable) result.sortable = col.sortable
	if (col.showOverflowTooltip !== false) result.showOverflowTooltip = true
	if (col.formatter) result.formatter = col.formatter
	return result
}

// ====== Refs ======
const tableRef = ref()
const cardRef = ref()

defineExpose({ tableRef, cardRef })

onUnmounted(() => {
	sortableInstance?.destroy()
	sortableInstance = null
})
</script>

<style scoped>
/* ====== 工具栏 ====== */
.table-tool-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.table-tool-bar-left,
.table-tool-bar-right {
	display: flex;
	align-items: center;
}
.table-tool-bar-right :deep(.icon-button) {
	background-color: var(--el-fill-color);
	border-radius: 6px;
	padding: 6px;
}
.table-tool-bar-right :deep(.icon-button:not(.is-disabled):hover) {
	background-color: var(--el-fill-color-dark);
}
:deep(.el-divider--vertical) {
	margin: 0 0;
}

/* ====== 列设置面板 ====== */
.column-settings-trigger {
	display: inline-flex;
	align-items: center;
}
.column-settings-panel {
	user-select: none;
}
.column-settings-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding-bottom: 8px;
	margin-bottom: 8px;
	border-bottom: 1px solid var(--el-border-color-lighter);
}
.column-settings-title {
	font-weight: 500;
	font-size: 14px;
	color: var(--el-text-color-primary);
}
.column-list {
	display: flex;
	flex-direction: column;
	gap: 2px;
}
.column-list-item {
	display: flex;
	align-items: center;
	gap: 8px;
	padding: 4px 8px;
	border-radius: 4px;
	cursor: grab;
	transition: background-color 0.15s;
}
.column-list-item:hover {
	background-color: var(--el-fill-color-light);
}
.column-list-item.is-fixed {
	cursor: default;
	opacity: 0.7;
}
.column-list-item.is-fixed:hover {
	background-color: transparent;
}
.column-list-item .drag-handle {
	display: flex;
	align-items: center;
	color: var(--el-text-color-placeholder);
	flex-shrink: 0;
}
.column-list-item.is-fixed .drag-handle {
	color: var(--el-text-color-disabled);
}
.column-list-item.is-editing .drag-handle,
.column-list-item.is-editing .pin-btn,
.column-list-item.is-editing .rename-btn {
	display: none;
}
.rename-input {
	flex: 1;
	min-width: 0;
}
.rename-btns {
	display: flex;
	gap: 2px;
	flex-shrink: 0;
}
.rename-btn {
	opacity: 0;
	transition: opacity 0.2s;
	flex-shrink: 0;
}
.column-list-item:hover .rename-btn {
	opacity: 1;
}
.pin-btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 22px;
	height: 22px;
	border: none;
	background: transparent;
	color: var(--el-text-color-secondary);
	cursor: pointer;
	border-radius: 3px;
	flex-shrink: 0;
}
.pin-btn:hover {
	background-color: var(--el-fill-color);
}
.pin-btn.is-active {
	color: var(--el-color-primary);
}
</style>
```

- [ ] **Step 2: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "DataTable"`
Expected: 无新增错误

- [ ] **Step 3: Commit**

```bash
git add src/components/crud/DataTable.vue
git commit -m "feat(crud): add DataTable component with toolbar and column settings panel"
```


---

### Task 8: QueryTable

**Files:**
- Create: `src/components/crud/QueryTable.vue`

**Interfaces:**
- Consumes: DataTable, SearchForm
- `v-model:queryForm` → 查询表单数据
- `v-model:showSearch` → 搜索显隐
- `v-model:maximized` → 最大化
- `v-model:columns` → 列配置
- 插槽转发：`#toolbar-left`、`#operation`、`#{column.key}`

- [ ] **Step 1: 创建 `src/components/crud/QueryTable.vue`**

```vue
<template>
	<div>
		<!-- 搜索表单 -->
		<search-form
			:fields="searchFields"
			:visible="showSearch"
			:loading="tableLoading"
			v-model="queryForm"
			@search="emit('getDataList')"
			@reset="emit('resetQuery')"
		>
			<!-- 透传搜索字段插槽 -->
			<template v-for="(_, name) in $slots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}" />
			</template>
		</search-form>

		<!-- 表格 -->
		<data-table
			v-model:show-search="showSearch"
			v-model:query-loading="tableLoading"
			v-model:maximized="maximized"
			v-model:columns="columns"
			:data="data"
			:loading="loading"
			:pagination="pagination"
			:page-sizes="pageSizes"
			:max-height="maxHeight"
			@selection-change="(s: any[]) => emit('selectionChange', s)"
			@sort-change="(s: any) => emit('sortChange', s)"
			@page-change="(p: number) => emit('pageChange', p)"
			@size-change="(s: number) => emit('sizeChange', s)"
			@get-data-list="emit('getDataList')"
			@reset-columns="emit('resetColumns')"
		>
			<!-- 工具栏左侧 -->
			<template #toolbar-left>
				<slot name="toolbar-left" />
			</template>
			<!-- 操作列 -->
			<template #operation="scope">
				<slot name="operation" v-bind="scope" />
			</template>
			<!-- 透传表格列插槽 -->
			<template v-for="(_, name) in columnSlots" :key="name" #[name]="scope">
				<slot :name="name" v-bind="scope" />
			</template>
		</data-table>
	</div>
</template>

<script setup lang="ts">
import type { SearchFieldSchema, TableColumnSchema, Pagination } from '@/types/schema'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'

defineOptions({
	name: 'QueryTable'
})

// ====== defineModel ======
const queryForm = defineModel<Record<string, any>>('queryForm', { required: true })
const showSearch = defineModel<boolean>('showSearch', { default: true })
const maximized = defineModel<boolean>('maximized', { default: false })
const columns = defineModel<TableColumnSchema[]>('columns', { default: () => [] })

defineProps<{
	searchFields: SearchFieldSchema[]
	data: any[]
	loading?: boolean
	tableLoading?: boolean
	pagination?: Pagination
	pageSizes?: number[]
	maxHeight?: string
}>()

const emit = defineEmits<{
	(e: 'getDataList'): void
	(e: 'resetQuery'): void
	(e: 'selectionChange', selections: any[]): void
	(e: 'sortChange', sort: any): void
	(e: 'pageChange', page: number): void
	(e: 'sizeChange', size: number): void
	(e: 'resetColumns'): void
}>()

// 过滤出表格列插槽（排除已知非列插槽）
const knownSlots = new Set(['toolbar-left', 'operation'])
const columnSlots = computed(() => {
	const slots: Record<string, any> = {}
	for (const name in useSlots()) {
		if (!knownSlots.has(name)) {
			slots[name] = true
		}
	}
	return slots
})
</script>
```

- [ ] **Step 2: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "QueryTable"`
Expected: 无新增错误

- [ ] **Step 3: Commit**

```bash
git add src/components/crud/QueryTable.vue
git commit -m "feat(crud): add QueryTable component aggregating SearchForm and DataTable"
```

---

### Task 9: CrudDialog

**Files:**
- Create: `src/components/crud/CrudDialog.vue`

**Interfaces:**
- Consumes: FormField, `FormFieldSchema`, `FormMode`
- `v-model:visible` → 弹窗显隐
- Props: `mode`、`formSchema`、`formData`、`rules`、`labelWidth`、`subject`、`submitLoading`
- Events: `@submit`、`@close`

- [ ] **Step 1: 创建 `src/components/crud/CrudDialog.vue`**

```vue
<template>
	<el-dialog v-model="visible" :title="title" :close-on-click-modal="false">
		<el-form ref="formRef" :model="formData" :rules="rules" :label-width="labelWidth" @keyup.enter="handleSubmit">
			<form-field
				v-for="field in formSchema"
				:key="field.prop"
				v-model="formData"
				:field="field"
				:mode="mode"
			>
				<!-- 透传插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
				<template v-for="(_, name) in passthroughSlots" :key="name" #[name]="slotData">
					<slot :name="name" v-bind="slotData ?? {}" />
				</template>
			</form-field>
		</el-form>
		<template #footer>
			<template v-if="mode !== 'detail'">
				<el-button type="primary" :icon="Check" :loading="submitLoading" @click="handleSubmit">确定</el-button>
				<el-button :icon="Close" @click="visible = false">取消</el-button>
			</template>
			<template v-else>
				<el-button :icon="Close" @click="visible = false">关闭</el-button>
			</template>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { Check, Close } from '@element-plus/icons-vue'
import type { FormFieldSchema, FormMode } from '@/types/schema'
import type { FormItemRule } from 'element-plus'
import FormField from './internal/FormField.vue'

defineOptions({
	name: 'CrudDialog'
})

const visible = defineModel<boolean>('visible', { default: false })

const props = withDefaults(
	defineProps<{
		mode: FormMode
		formSchema: FormFieldSchema[]
		formData: Record<string, any>
		rules?: Record<string, FormItemRule[]>
		labelWidth?: string
		subject?: string
		submitLoading?: boolean
	}>(),
	{
		labelWidth: '100px',
		subject: '数据',
		submitLoading: false
	}
)

const emit = defineEmits<{
	(e: 'submit'): void
}>()

const formRef = ref()

// 标题
const titleMap: Record<string, string> = { add: '新增', update: '修改', detail: '' }
const title = computed(() => {
	if (props.mode === 'detail') return `${props.subject}详情`
	return `${titleMap[props.mode] ?? '操作'}${props.subject}`
})

// 提交（内部校验，通过后才 emit）
const handleSubmit = () => {
	formRef.value?.validate((valid: boolean) => {
		if (valid) {
			emit('submit')
		}
	})
}

// 暴露 formRef
defineExpose({ formRef })

// 透传插槽（排除保留插槽，直接转发给 FormField）
const reservedSlotNames = new Set(['toolbar-left', 'action-column'])
const passthroughSlots = computed(() => {
	const slots: Record<string, any> = {}
	for (const name in useSlots()) {
		if (!reservedSlotNames.has(name)) {
			slots[name] = true
		}
	}
	return slots
})
</script>
```

- [ ] **Step 2: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "CrudDialog"`
Expected: 无新增错误

- [ ] **Step 3: Commit**

```bash
git add src/components/crud/CrudDialog.vue
git commit -m "feat(crud): add CrudDialog component for add/update/detail form dialog"
```

---

### Task 10: CrudPage

**Files:**
- Create: `src/components/crud/CrudPage.vue`

**Interfaces:**
- Consumes: QueryTable, CrudDialog, useCrudTable, useCrudForm
- 所有 schema/api props 如设计文档定义
- 插槽：`#toolbar-left`、`#action-column`、`#field-{prop}`

- [ ] **Step 1: 创建 `src/components/crud/CrudPage.vue`**

```vue
<template>
	<query-table
		v-model:query-form="table.search.form"
		v-model:show-search="table.search.visible"
		v-model:maximized="table.refs.cardMaximized"
		v-model:columns="table.columnConfig.list"
		:search-fields="table.search.fields"
		:data="table.table.data"
		:loading="table.table.loading"
		:table-loading="table.table.loading"
		:pagination="table.table.pagination"
		:page-sizes="pageSizes"
		@get-data-list="table.query.getDataList()"
		@reset-query="table.query.reset()"
		@selection-change="table.selection.onChange"
		@sort-change="table.query.onSortChange"
		@page-change="table.query.onPageChange"
		@size-change="table.query.onSizeChange"
		@reset-columns="table.columnConfig.reset()"
	>
		<!-- 工具栏左侧：默认新增+批量删除 -->
		<template #toolbar-left>
			<slot name="toolbar-left" :table="table" :form="form">
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="form.dialog.open('add')">新增</el-button>
					<el-button type="danger" :loading="table.delete.loading" :icon="Delete" @click="table.delete.execute()">删除</el-button>
				</el-space>
			</slot>
		</template>
		<!-- 操作列：默认修改+行删除 -->
		<template #operation="{ row }">
			<slot name="action-column" :row="row" :table="table" :form="form">
				<el-button type="primary" link :icon="Edit" @click="form.dialog.open('update', row[primaryKey])">修改</el-button>
				<el-button type="primary" link :icon="Delete" @click="table.delete.execute(row)">删除</el-button>
			</slot>
		</template>
	</query-table>

	<!-- 弹窗表单 -->
	<crud-dialog
		v-model:visible="form.dialog.visible"
		:mode="form.dialog.mode"
		:form-schema="formSchema"
		:form-data="form.form.data"
		:rules="form.form.rules"
		:label-width="form.form.labelWidth"
		:subject="subject"
		:submit-loading="form.submit.loading"
		@submit="form.submit.execute()"
	>
		<!-- 透传所有插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
		<template v-for="(_, name) in dialogSlots" :key="name" #[name]="slotData">
			<slot :name="name" v-bind="slotData ?? {}" />
		</template>
	</crud-dialog>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'
import type { SearchFieldSchema, FormFieldSchema, TableColumnSchema } from '@/types/schema'
import type { Key, PageVO } from '@/types'
import { useCrudTable } from '@/hooks/use-crud-table'
import { useCrudForm } from '@/hooks/use-crud-form'
import QueryTable from './QueryTable.vue'
import CrudDialog from './CrudDialog.vue'

defineOptions({
	name: 'CrudPage'
})

// ====== Props ======
const props = withDefaults(
	defineProps<{
		searchSchema?: SearchFieldSchema[]
		formSchema: FormFieldSchema[]
		columns: TableColumnSchema[]
		dataListApi: (query: any) => Promise<PageVO<any>>
		deleteApi?: (ids: Key[]) => Promise<void>
		submitApi: (data: any) => Promise<any>
		detailApi?: (id: Key) => Promise<any>
		initQueryForm: () => any
		initFormData: () => any
		pageSize?: number
		pageSizes?: number[]
		subject?: string
		deleteNameKey?: string
		primaryKey?: string
		labelWidth?: string
		rules?: Record<string, FormItemRule[]>
		tableKey?: string
		submitAfter?: (data: any) => void
	}>(),
	{
		searchSchema: () => [],
		pageSize: 10,
		pageSizes: () => [10, 20, 50, 100, 200],
		subject: '数据',
		primaryKey: 'id',
		labelWidth: '100px'
	}
)

// ====== Hooks ======
const table = useCrudTable({
	dataListApi: props.dataListApi,
	deleteApi: props.deleteApi,
	initQueryForm: props.initQueryForm,
	columns: props.columns,
	searchSchema: props.searchSchema,
	pageSize: props.pageSize,
	pageSizes: props.pageSizes,
	primaryKey: props.primaryKey,
	subject: props.subject,
	deleteNameKey: props.deleteNameKey,
	tableKey: props.tableKey
})

const form = useCrudForm({
	submitApi: props.submitApi,
	detailApi: props.detailApi,
	initFormData: props.initFormData,
	subject: props.subject,
	rules: props.rules,
	labelWidth: props.labelWidth,
	submitAfter: props.submitAfter ?? (() => table.query.getDataList())
})

// ====== 插槽透传计算 ======
const reservedSlots = new Set(['toolbar-left', 'action-column'])
const dialogSlots = computed(() => {
	const slots: Record<string, any> = {}
	for (const name in useSlots()) {
		if (!reservedSlots.has(name)) {
			slots[name] = true
		}
	}
	return slots
})
</script>
```

- [ ] **Step 2: 验证类型**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "CrudPage"`
Expected: 无新增错误

- [ ] **Step 3: Commit**

```bash
git add src/components/crud/CrudPage.vue
git commit -m "feat(crud): add CrudPage top-level component with built-in hooks"
```

---

### Task 11: 测试页面 + 验证

**Files:**
- Create: `src/views/gen/field-type/test.vue`

**Interfaces:**
- Consumes: CrudPage, genFieldTypeApi, ATTR_TYPES

- [ ] **Step 1: 创建 `src/views/gen/field-type/test.vue`**

```vue
<template>
	<crud-page
		:search-schema="searchSchema"
		:form-schema="formSchema"
		:columns="columns"
		:data-list-api="genFieldTypeApi.entityPage"
		:delete-api="genFieldTypeApi.deleteList"
		:submit-api="genFieldTypeApi.submit"
		:detail-api="genFieldTypeApi.detail"
		:init-query-form="() => ({ columnType: '', attrType: '' })"
		:init-form-data="() => ({ id: null, columnType: '', attrType: '', packageName: '' })"
		subject="字段类型"
		delete-name-key="columnType"
	/>
</template>

<script setup lang="ts">
import { genFieldTypeApi } from '@/api'
import { ATTR_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import type { SearchFieldSchema, FormFieldSchema, TableColumnSchema } from '@/types/schema'
import CrudPage from '@/components/crud/CrudPage.vue'

defineOptions({
	name: 'GenFieldTypeTest'
})

const searchSchema: SearchFieldSchema[] = [
	{ prop: 'columnType', label: '字段类型' },
	{ prop: 'attrType', label: '属性类型', component: 'select', options: ATTR_TYPES.items, filterable: true }
]

const formSchema: FormFieldSchema[] = [
	{ prop: 'columnType', label: '字段类型' },
	{ prop: 'attrType', label: '属性类型', component: 'select', options: ATTR_TYPES.items, filterable: true },
	{ prop: 'packageName', label: '包名' }
]

const columns: TableColumnSchema[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'columnType', prop: 'columnType', label: '字段类型', align: 'center' },
	{ key: 'attrType', prop: 'attrType', label: '属性类型', align: 'center', formatter: getLabel(ATTR_TYPES) },
	{ key: 'packageName', prop: 'packageName', label: '包名' },
	{ key: 'createTime', prop: 'createTime', label: '创建时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'updateTime', prop: 'updateTime', label: '修改时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'operation', label: '操作', fixed: 'right' as const, width: 150, align: 'center', disabled: true }
]
</script>
```

- [ ] **Step 2: 全量类型检查**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit`
Expected: 22 个既有错误（与基线一致），新增文件零新增错误

- [ ] **Step 3: 启动开发服务器验证**

Run: `cd yanggu-code-generator-frontend && pnpm dev`
Expected: 编译成功，无报错

- [ ] **Step 4: 浏览器验证**

导航到 test.vue 对应的路由，验证：
1. 搜索表单正常渲染，查询/重置按钮可用
2. 表格正常加载数据，分页可用
3. 工具栏按钮（搜索切换/刷新/列设置/最大化）正常
4. 列设置面板：显隐/固定/拖拽排序/重命名 均正常
5. 新增弹窗正常打开，表单字段正常渲染
6. 修改弹窗正常加载详情数据
7. 详情弹窗正常展示纯文本
8. 提交成功后弹窗关闭，表格自动刷新
9. 删除确认弹窗正常，删除后表格自动刷新

- [ ] **Step 5: Commit**

```bash
git add src/views/gen/field-type/test.vue
git commit -m "feat(crud): add test page for CRUD component system"
```

---

## Verification Checklist

- [ ] vue-tsc 基线 22 个既有错误，新增 13 个文件零新增错误
- [ ] 开发服务器启动无编译错误
- [ ] 搜索表单：查询/重置可用，select 类型字段正常
- [ ] 表格：数据加载/分页/排序正常
- [ ] 列设置：显隐/固定/拖拽/重命名/重置 均正常
- [ ] 弹窗：新增/修改/详情 三种模式正常
- [ ] 表单提交后表格自动刷新
- [ ] 删除确认 + 批量删除正常
