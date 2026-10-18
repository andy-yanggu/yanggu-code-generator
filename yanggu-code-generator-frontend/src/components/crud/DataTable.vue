<template>
	<el-card ref="cardRef" class="layout-table-card" :class="{ 'is-maximized': maximized }" shadow="hover">
		<!-- 工具栏 -->
		<template #header>
			<div class="table-tool-bar">
				<div v-if="showToolbarLeft" class="table-tool-bar-left">
					<slot name="toolbar-left"></slot>
				</div>
				<div class="table-tool-bar-right">
					<el-space size="small" :spacer="spacer">
						<!-- 搜索 -->
						<icon-button size="16px" :el-icon="Search" :tooltip="showSearch ? '隐藏搜索' : '显示搜索'" @click="toggleSearch()" />
						<!-- 刷新 -->
						<icon-button size="16px" :el-icon="Refresh" tooltip="刷新表格" @click="emit('getDataList')" />
						<!-- 列设置 -->
						<el-popover
							v-model:visible="columnSettingsVisible"
							placement="top"
							:width="300"
							trigger="click"
							popper-class="yanggu-column-settings-popover"
							@show="handlePopoverShow"
							@hide="handlePopoverHide"
						>
							<template #reference>
								<span class="column-settings-trigger">
									<icon-button size="16px" :el-icon="Setting" tooltip="列设置" />
								</span>
							</template>
							<div class="column-settings-panel">
								<div class="column-settings-header">
									<span class="column-settings-title">列设置</span>
									<el-button link type="primary" size="small" @click="resetColumns">重置</el-button>
								</div>
								<div ref="columnListRef" class="column-list">
									<template v-for="col in columns" :key="col.key">
										<!-- 分组：表头行 + 组内叶子容器（独立 Sortable，禁止跨组移动） -->
										<div v-if="col.children?.length" class="column-group" :class="{ 'is-fixed': col.fixed || col.disabled }">
											<column-setting-item
												:col="col"
												is-group
												:editing="editingKey === col.key"
												:editing-title="editingTitle"
												@update:editing-title="editingTitle = $event"
												@toggle-visible="val => toggleColumnVisible(col, val)"
												@toggle-fixed="dir => toggleFixed(col, dir)"
												@start-rename="startRename(col)"
												@save-rename="saveRename()"
												@cancel-rename="cancelRename()"
											/>
											<div class="column-group-children" :data-group-key="col.key">
												<column-setting-item
													v-for="child in col.children"
													:key="child.key"
													:col="child"
													indent
													:show-fixed="false"
													:editing="editingKey === child.key"
													:editing-title="editingTitle"
													@update:editing-title="editingTitle = $event"
													@toggle-visible="val => toggleColumnVisible(child, val)"
													@start-rename="startRename(child)"
													@save-rename="saveRename()"
													@cancel-rename="cancelRename()"
												/>
											</div>
										</div>
										<!-- 顶层叶子列 -->
										<column-setting-item
											v-else
											:col="col"
											:editing="editingKey === col.key"
											:editing-title="editingTitle"
											@update:editing-title="editingTitle = $event"
											@toggle-visible="val => toggleColumnVisible(col, val)"
											@toggle-fixed="dir => toggleFixed(col, dir)"
											@start-rename="startRename(col)"
											@save-rename="saveRename()"
											@cancel-rename="cancelRename()"
										/>
									</template>
								</div>
							</div>
						</el-popover>
						<!-- 最大化 -->
						<icon-button size="16px" :el-icon="FullScreen" :tooltip="maximized ? '退出全屏' : '表格全屏'" @click="handleToggleMaximized()" />
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
			:row-key="rowKey"
			@selection-change="(s: any[]) => emit('selectionChange', s)"
			@sort-change="(s: any) => emit('sortChange', s)"
		>
			<template v-for="col in visibleColumns" :key="col.key">
				<!-- 分组列：父表头 + 内嵌叶子列（仅 2 层） -->
				<el-table-column v-if="col.children?.length" v-bind="toColumnProps(col)">
					<template v-if="col.tooltip" #header>
						<form-label-tooltip :label="col.label" :tooltip="col.tooltip" :align="col.headerAlign ?? col.align ?? 'center'" />
					</template>
					<el-table-column v-for="child in col.children" :key="child.key" v-bind="toColumnProps(child)">
						<template v-if="child.tooltip" #header>
							<form-label-tooltip :label="child.label" :tooltip="child.tooltip" :align="child.headerAlign ?? child.align ?? 'center'" />
						</template>
						<template v-if="child.key === 'operation'" #default="scope">
							<slot name="operation" v-bind="scope"></slot>
						</template>
						<template v-else-if="$slots[child.key]" #default="scope">
							<slot :name="child.key" v-bind="scope"></slot>
						</template>
					</el-table-column>
				</el-table-column>
				<!-- 顶层叶子列 -->
				<el-table-column v-else v-bind="toColumnProps(col)">
					<template v-if="col.tooltip" #header>
						<form-label-tooltip :label="col.label" :tooltip="col.tooltip" :align="col.headerAlign ?? col.align ?? 'center'" />
					</template>
					<template v-if="col.key === 'operation'" #default="scope">
						<slot name="operation" v-bind="scope"></slot>
					</template>
					<template v-else-if="$slots[col.key]" #default="scope">
						<slot :name="col.key" v-bind="scope"></slot>
					</template>
				</el-table-column>
			</template>
			<template #empty>
				<slot name="empty">
					<el-empty description="暂无数据" :image-size="120"></el-empty>
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
		></el-pagination>
	</el-card>
</template>

<script setup lang="ts">
import { FullScreen, Refresh, Search, Setting } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { ElDivider } from 'element-plus'
import IconButton from '@/components/icon-button/index.vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import ColumnSettingItem from './internal/ColumnSettingItem.vue'
import { defaultTableColumnOptions } from '@/hooks/use-crud-table'
import type { Pagination, TableColumnSchema } from '@/types/schema'

defineOptions({
	name: 'CrudDataTable',
	inheritAttrs: false
})

// ====== defineModel ======
const showSearch = defineModel('showSearch', { type: Boolean, default: true })
const maximized = defineModel('maximized', { type: Boolean, default: false })
const columns = defineModel<TableColumnSchema[]>('columns', { default: () => [] })

const props = withDefaults(
	defineProps<{
		data: any[]
		loading?: boolean
		pagination?: Pagination
		pageSizes?: number[]
		maxHeight?: string
		/**
		 * 序号列的行号计算函数，映射到 el-table-column 的 `index` 属性。
		 * el-table 的 type="index" 只按「当前页」的行下标从 1 起算，翻页后又从 1 开始，
		 * 必须由外部传入跨页序号函数（(pageNum-1)*pageSize+index+1）才能与分页条页码对齐。
		 */
		rowIndex?: (index: number) => number
		/** 行数据主键字段名，映射到 el-table 的 row-key（跳页保持勾选、树形数据行身份依赖它）。默认 'id' */
		rowKey?: string
		/** 是否显示工具栏左侧（默认按钮或用户插槽）。默认 true */
		showToolbarLeft?: boolean
	}>(),
	{
		loading: false,
		pageSizes: () => [10, 20, 50, 100, 200],
		maxHeight: '60vh',
		// 不传时等同 el-table 原生行为：每页从 1 开始
		rowIndex: (index: number) => index + 1,
		rowKey: 'id',
		showToolbarLeft: true
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

// ====== 可见列（递归：分组下无可见叶子时整组隐藏） ======
const isColumnVisible = (col: TableColumnSchema) => col.visible !== false
const visibleColumns = computed<TableColumnSchema[]>(() => {
	const result: TableColumnSchema[] = []
	for (const col of columns.value) {
		if (!isColumnVisible(col)) continue
		if (col.children?.length) {
			const children = col.children.filter(isColumnVisible)
			if (children.length) result.push({ ...col, children })
		} else {
			result.push(col)
		}
	}
	return result
})

// ====== 列设置面板 ======
const columnSettingsVisible = ref(false)
const columnListRef = ref<HTMLElement>()
// 根 Sortable（顶层排序）+ 每个分组一个子 Sortable（组内排序，pull/put=false 禁止跨组）
let sortableInstances: Sortable[] = []

// 重命名状态
const editingKey = ref<string | null>(null)
const editingTitle = ref('')

const handlePopoverShow = () => {
	nextTick(() => createSortable())
}

const destroySortables = () => {
	sortableInstances.forEach(s => s.destroy())
	sortableInstances = []
}

const handlePopoverHide = () => {
	destroySortables()
}

// 通用重排：把 oldIndex 处的元素移到 newIndex
const reorder = (arr: TableColumnSchema[], oldIndex: number, newIndex: number): TableColumnSchema[] => {
	const next = [...arr]
	const [moved] = next.splice(oldIndex, 1)
	next.splice(newIndex, 0, moved)
	return next
}

const createSortable = () => {
	const el = columnListRef.value
	if (!el) return
	destroySortables()

	// 根：只重排顶层（分组容器 + 未分组叶子），保护固定列区
	sortableInstances.push(
		new Sortable(el, {
			animation: 150,
			// .column-group-children：忽略组内叶子的拖拽（交给各自的子 Sortable），避免根实例把整组拖走
			filter: '.is-fixed, .column-group-children',
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
				columns.value = reorder(columns.value, oldIndex, newIndex)
			}
		})
	)

	// 每个分组的子列表：组内重排，pull/put=false 从机制上禁止跨组移动
	el.querySelectorAll<HTMLElement>('.column-group-children').forEach(childEl => {
		const groupKey = childEl.dataset.groupKey
		if (!groupKey) return
		sortableInstances.push(
			new Sortable(childEl, {
				animation: 150,
				group: { name: `grp-${groupKey}`, pull: false, put: false },
				filter: '.is-fixed',
				preventOnFilter: false,
				onEnd: evt => {
					const { oldIndex, newIndex } = evt
					if (oldIndex == null || newIndex == null || oldIndex === newIndex) return
					columns.value = columns.value.map(c => (c.key === groupKey ? { ...c, children: reorder(c.children ?? [], oldIndex, newIndex) } : c))
				}
			})
		)
	})
}

// 递归定位并更新指定 key 的列（不可变）；未命中返回原数组引用
const updateColumnByKey = (list: TableColumnSchema[], key: string, patch: (col: TableColumnSchema) => Partial<TableColumnSchema>): TableColumnSchema[] => {
	let changed = false
	const next = list.map(col => {
		if (col.key === key) {
			changed = true
			return { ...col, ...patch(col) }
		}
		if (col.children?.length) {
			const newChildren = updateColumnByKey(col.children, key, patch)
			if (newChildren !== col.children) {
				changed = true
				return { ...col, children: newChildren }
			}
		}
		return col
	})
	return changed ? next : list
}

const toggleColumnVisible = (col: TableColumnSchema, visible: boolean | string | number) => {
	columns.value = updateColumnByKey(columns.value, col.key, () => ({ visible: !!visible }))
}

const toggleFixed = (col: TableColumnSchema, direction: 'left' | 'right') => {
	columns.value = updateColumnByKey(columns.value, col.key, c => ({ fixed: c.fixed === direction ? false : direction }))
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
		columns.value = updateColumnByKey(columns.value, editingKey.value, () => ({ label: trimmed }))
	}
	editingKey.value = null
}

const cancelRename = () => {
	editingKey.value = null
}

// ====== 列配置转换 ======
const toColumnProps = (col: TableColumnSchema): Record<string, any> => {
	const defaults = defaultTableColumnOptions()
	const result: Record<string, any> = {}
	// 分组列：仅作父表头，不下发 prop / formatter / showOverflowTooltip / sortable（对纯表头无意义）
	if (col.children?.length) {
		if (col.label) result.label = col.label
		if (col.width) result.width = col.width
		if (col.minWidth) result.minWidth = col.minWidth
		if (col.fixed) result.fixed = col.fixed
		result.align = col.align ?? defaults.align
		if (col.headerAlign) result.headerAlign = col.headerAlign
		return result
	}
	if (col.type) {
		result.type = col.type
		// 序号列必须下发 index，否则 el-table 只按当前页下标从 1 数，第 2 页起仍是 1、2、3
		if (col.type === 'index') result.index = props.rowIndex
	}
	if (col.prop) result.prop = col.prop
	else if (col.key && !col.type) result.prop = col.key
	if (col.label) result.label = col.label
	if (col.width) result.width = col.width
	if (col.minWidth) result.minWidth = col.minWidth
	if (col.fixed) result.fixed = col.fixed
	result.align = col.align ?? defaults.align
	if (col.headerAlign) result.headerAlign = col.headerAlign
	if (col.sortable) result.sortable = col.sortable
	if ((col.showOverflowTooltip ?? defaults.showOverflowTooltip) !== false) result.showOverflowTooltip = true
	if (col.formatter) result.formatter = col.formatter
	if (col.reserveSelection) result.reserveSelection = true
	return result
}

// ====== Refs ======
const tableRef = ref()
const cardRef = ref()

defineExpose({ tableRef, cardRef })

onUnmounted(() => {
	destroySortables()
})
</script>

<style scoped>
/* ====== 工具栏 ====== */
.table-tool-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.table-tool-bar-left {
	display: flex;
	align-items: center;
}

.table-tool-bar-right {
	display: flex;
	align-items: center;
	margin-left: auto;
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
/* 分组容器：作为根 Sortable 的拖拽单元 */
.column-group {
	display: flex;
	flex-direction: column;
	gap: 2px;
	border-radius: 4px;
}
.column-group-children {
	display: flex;
	flex-direction: column;
	gap: 2px;
}
</style>
