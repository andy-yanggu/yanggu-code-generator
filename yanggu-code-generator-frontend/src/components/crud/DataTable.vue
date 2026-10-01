<template>
	<el-card ref="cardRef" class="layout-table-card" :class="{ 'is-maximized': maximized }" shadow="hover">
		<!-- 工具栏 -->
		<template #header>
			<div class="table-tool-bar">
				<div class="table-tool-bar-left">
          <slot name="toolbar-left"></slot>
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
						<icon-button size="16px" :el-icon="Refresh" tooltip="刷新表格" @click="emit('getDataList')" />
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
									<icon-button
										size="16px"
										:el-icon="Setting"
										tooltip="列设置"
										@click="columnSettingsVisible = !columnSettingsVisible"
									/>
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
												<button
													class="pin-btn"
													:class="{ 'is-active': col.fixed === 'left' }"
													title="固定在左边"
													@click="toggleFixed(col, 'left')"
												>
													<el-icon :size="14"><ArrowLeft /></el-icon>
												</button>
												<button
													class="pin-btn"
													:class="{ 'is-active': col.fixed === 'right' }"
													title="固定在右边"
													@click="toggleFixed(col, 'right')"
												>
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
import { ArrowLeft, ArrowRight, Check, Close, Edit, FullScreen, Lock, Rank, Refresh, Search, Setting } from '@element-plus/icons-vue'
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
const maximized = defineModel('maximized', { type: Boolean, default: false })
const columns = defineModel<TableColumnSchema[]>('columns', { default: () => [] })

withDefaults(
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
