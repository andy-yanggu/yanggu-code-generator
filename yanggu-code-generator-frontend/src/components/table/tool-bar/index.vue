<template>
	<div class="table-tool-bar">
		<!-- 左插槽：父组件传入 -->
		<div class="table-tool-bar-left">
			<slot name="left"></slot>
		</div>

		<!-- 右侧：通用工具 -->
		<div class="table-tool-bar-right">
			<el-space size="small" :spacer="spacer">
				<!-- 搜索 -->
				<icon-button v-if="useSearch" size="16px" :el-icon="Search" :tooltip="showSearch ? '隐藏搜索' : '显示搜索'" @click="toggleSearch()">
				</icon-button>

				<!-- 刷新 -->
				<icon-button
					v-if="useRefresh"
					size="16px"
					:el-icon="Refresh"
					tooltip="刷新表格"
					:loading="queryLoading"
					:loading-icon="Refresh"
					@click="emit('getDataList')"
				>
				</icon-button>

				<!-- 列设置 -->
				<el-popover
					v-if="useColumnSettings"
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
							<icon-button size="16px" :el-icon="Setting" tooltip="列设置" @click="columnSettingsVisible = !columnSettingsVisible"></icon-button>
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
								<!-- 编辑模式：输入框 + 确认/取消 -->
								<template v-if="editingKey === col.key">
									<span class="drag-handle">
										<el-icon :size="14"><Lock /></el-icon>
									</span>
									<el-input
										v-model="editingTitle"
										size="small"
										placeholder="请输入列名"
										class="rename-input"
										@click.stop
										@keydown.enter="saveRename()"
										@keydown.escape="cancelRename()"
									></el-input>
									<div class="rename-btns">
										<el-button type="primary" :icon="Check" link size="small" @click.stop="saveRename()"></el-button>
										<el-button :icon="Close" link size="small" @click.stop="cancelRename()"></el-button>
									</div>
								</template>
								<!-- 显示模式 -->
								<template v-else>
									<!-- 拖拽手柄：固定/禁用列显示锁定图标，普通列显示拖拽图标 -->
									<span class="drag-handle">
										<el-icon v-if="col.fixed || col.disabled" :size="14"><Lock /></el-icon>
										<el-icon v-else :size="14"><Rank /></el-icon>
									</span>
									<!-- 复选框：控制列显隐 -->
									<el-checkbox
										:model-value="col.visible !== false"
										:disabled="col.disabled"
										@change="(val: boolean | string | number) => toggleColumnVisible(col, val)"
									>
										{{ col.label }}
									</el-checkbox>
									<!-- 置左/置右按钮（禁用列不显示） -->
									<template v-if="!col.disabled">
										<button class="pin-btn" :class="{ 'is-active': col.fixed === 'left' }" title="固定在左边" @click="toggleFixed(col, 'left')">
											<el-icon :size="14"><ArrowLeft /></el-icon>
										</button>
										<button class="pin-btn" :class="{ 'is-active': col.fixed === 'right' }" title="固定在右边" @click="toggleFixed(col, 'right')">
											<el-icon :size="14"><ArrowRight /></el-icon>
										</button>
									</template>
									<!-- 重命名按钮（禁用列不显示，悬停出现） -->
									<el-button
										v-if="!col.disabled"
										:icon="Edit"
										type="primary"
										link
										size="small"
										class="rename-btn"
										title="重命名"
										@click.stop="startRename(col)"
									></el-button>
								</template>
							</div>
						</div>
					</div>
				</el-popover>

				<!-- 最大化 -->
				<icon-button
					v-if="useMaximize"
					size="16px"
					:svg-icon="maximized ? 'icon-fullscreen-exit' : 'icon-fullscreen'"
					:tooltip="maximized ? '退出全屏' : '表格全屏'"
					@click="handleToggleMaximized()"
				></icon-button>
			</el-space>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ArrowLeft, ArrowRight, Check, Close, Edit, Lock, Rank, Refresh, Search, Setting } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import IconButton from '@/components/icon-button/index.vue'
import type { ColumnConfig } from '@/types/hooks/column-config'
import { ElDivider } from 'element-plus'

defineOptions({
	name: 'TableToolBar'
})

// 查询区域是否显示
const showSearch = defineModel('showSearch', {
	type: Boolean,
	default: true
})

// 查询加载中状态
const queryLoading = defineModel('queryLoading', {
	type: Boolean,
	default: false
})

// 表格卡片是否最大化
const maximized = defineModel('maximized', {
	type: Boolean,
	default: false
})

// 列配置（双向绑定，用于列设置面板）
const columns = defineModel<ColumnConfig[]>('columns', {
	default: () => []
})

defineProps({
	// 是否显示搜索按钮
	useSearch: {
		type: Boolean,
		default: true
	},
	// 是否显示刷新按钮
	useRefresh: {
		type: Boolean,
		default: true
	},
	// 是否显示最大化按钮
	useMaximize: {
		type: Boolean,
		default: true
	},
	// 是否显示列设置按钮
	useColumnSettings: {
		type: Boolean,
		default: false
	}
})

const spacer = h(ElDivider, { direction: 'vertical' })

const emit = defineEmits<{
	(e: 'getDataList'): void
	(e: 'resetColumns'): void
}>()

const toggleSearch = useToggle(showSearch)
const toggleMaximized = useToggle(maximized)

// ====== 列设置 ======
const columnSettingsVisible = ref(false)
const columnListRef = ref<HTMLElement>()
let sortableInstance: Sortable | null = null

// 重命名状态
const editingKey = ref<string | null>(null)
const editingTitle = ref('')

// Popover 显示时初始化拖拽
const handlePopoverShow = () => {
	nextTick(() => {
		createSortable()
	})
}

// Popover 隐藏时销毁拖拽
const handlePopoverHide = () => {
	sortableInstance?.destroy()
	sortableInstance = null
}

// 创建 Sortable 实例
const createSortable = () => {
	const el = columnListRef.value
	if (!el) return

	// 销毁旧实例
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

			// 固定列保护：不允许非固定列拖入固定列区域
			const draggedCol = cols[draggedIdx]
			if (draggedCol?.fixed) return true

			const fixedLeftCount = cols.filter((c: ColumnConfig) => c.fixed === 'left').length
			const fixedRightCount = cols.filter((c: ColumnConfig) => c.fixed === 'right').length
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

// 切换列显隐
const toggleColumnVisible = (col: ColumnConfig, visible: boolean | string | number) => {
	const idx = columns.value.findIndex((c: ColumnConfig) => c.key === col.key)
	if (idx === -1) return
	const updated = [...columns.value]
	updated[idx] = { ...updated[idx], visible: !!visible }
	columns.value = updated
}

// 切换列固定方向（left / right / 取消固定）
const toggleFixed = (col: ColumnConfig, direction: 'left' | 'right') => {
	const idx = columns.value.findIndex(c => c.key === col.key)
	if (idx === -1) return
	const updated = [...columns.value]
	updated[idx] = { ...updated[idx], fixed: col.fixed === direction ? false : direction }
	columns.value = updated
}

// 重置列设置（通知父组件执行重置，父组件更新 columns 后通过 v-model 回传）
const resetColumns = () => {
	editingKey.value = null
	emit('resetColumns')
}

// ====== 列重命名 ======
const startRename = (col: ColumnConfig) => {
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

// 最大化切换，进入最大化时提示 ESC 退出
const handleToggleMaximized = () => {
	toggleMaximized()
	if (!maximized.value) {
		ElMessage.info({ message: '按 ESC 可退出最大化', duration: 1000 })
	}
}

// ESC 退出最大化
onKeyStroke('Escape', () => {
	if (maximized.value) {
		maximized.value = false
	}
})

// 组件卸载时清理
onUnmounted(() => {
	sortableInstance?.destroy()
	sortableInstance = null
})
</script>

<style scoped>
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

/* 编辑模式：隐藏拖拽手柄和置左置右按钮 */
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

.column-list-item :deep(.el-checkbox) {
	flex: 1;
	margin-right: 0;
}

/* 置左/置右按钮 */
.pin-btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 24px;
	height: 24px;
	padding: 0;
	border: none;
	border-radius: 4px;
	background: transparent;
	color: var(--el-text-color-placeholder);
	cursor: pointer;
	transition:
		color 0.15s,
		background-color 0.15s;
	flex-shrink: 0;
}

.pin-btn:hover {
	color: var(--el-text-color-secondary);
	background-color: var(--el-fill-color);
}

.pin-btn.is-active {
	color: var(--el-color-primary);
}

.pin-btn.is-active:hover {
	color: var(--el-color-primary-light-3);
	background-color: var(--el-color-primary-light-9);
}
</style>

<!-- 列设置 Popover 全局样式（ElPopover 渲染在 body 下，无法使用 scoped） -->
<style>
.yanggu-column-settings-popover {
	padding: 12px 16px !important;
}
</style>
