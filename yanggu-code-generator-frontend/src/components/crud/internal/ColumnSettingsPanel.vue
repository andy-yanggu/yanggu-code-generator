<!--
	ColumnSettingsPanel：列设置面板（从 DataTable 拆出）
	- 接收 v-model:columns 双向绑定，内部直接修改 columns.value
	- Sortable 拖拽排序（根 + 分组子实例）、列显隐、固定、重命名、重置
	- 通过 defineExpose 暴露 onPopoverShow / resetState 供父组件调用
-->
<template>
	<div class="column-settings-panel">
		<div class="column-settings-header">
			<span class="column-settings-title">列设置</span>
			<el-button link type="primary" size="small" @click="handleReset">重置</el-button>
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
						@update:editing-title="val => editingTitle = val"
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
							@update:editing-title="val => editingTitle = val"
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
					@update:editing-title="val => editingTitle = val"
					@toggle-visible="val => toggleColumnVisible(col, val)"
					@toggle-fixed="dir => toggleFixed(col, dir)"
					@start-rename="startRename(col)"
					@save-rename="saveRename()"
					@cancel-rename="cancelRename()"
				/>
			</template>
		</div>
	</div>
</template>

<script setup lang="ts">
import { nextTick, onUnmounted, ref } from 'vue'
import Sortable from 'sortablejs'
import type { TableFieldSchema } from '@/types/schema'
import ColumnSettingItem from './ColumnSettingItem.vue'

defineOptions({
	name: 'ColumnSettingsPanel'
})

const columns = defineModel<TableFieldSchema[]>('columns', { required: true })

const emit = defineEmits<{
	(e: 'reset'): void
}>()

// ====== 内部状态 ======
const columnListRef = ref<HTMLElement>()
let sortableInstances: Sortable[] = []
const editingKey = ref<string | null>(null)
const editingTitle = ref('')

// ====== Sortable ======
const destroySortables = () => {
	sortableInstances.forEach(s => s.destroy())
	sortableInstances = []
}

// 通用重排：把 oldIndex 处的元素移到 newIndex
const reorder = (arr: TableFieldSchema[], oldIndex: number, newIndex: number): TableFieldSchema[] => {
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
				const fixedLeftCount = cols.filter((c: TableFieldSchema) => c.fixed === 'left').length
				const fixedRightCount = cols.filter((c: TableFieldSchema) => c.fixed === 'right').length
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
					columns.value = columns.value.map(c =>
						c.key === groupKey ? { ...c, children: reorder(c.children ?? [], oldIndex, newIndex) } : c
					)
				}
			})
		)
	})
}

// ====== 列操作 ======
// 递归查找并更新指定 key 的列（不可变）；未命中返回原数组引用
const updateColumnByKey = (
	list: TableFieldSchema[],
	key: string,
	patch: (col: TableFieldSchema) => Partial<TableFieldSchema>
): TableFieldSchema[] => {
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

const toggleColumnVisible = (col: TableFieldSchema, visible: boolean | string | number) => {
	columns.value = updateColumnByKey(columns.value, col.key, () => ({ visible: !!visible }))
}

const toggleFixed = (col: TableFieldSchema, direction: 'left' | 'right') => {
	columns.value = updateColumnByKey(columns.value, col.key, c => ({ fixed: c.fixed === direction ? false : direction }))
}

const startRename = (col: TableFieldSchema) => {
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

// ====== 重置 ======
const handleReset = () => {
	editingKey.value = null
	emit('reset')
}

// ====== 暴露方法 ======
/** popover 展示后调用，创建 Sortable 实例 */
const onPopoverShow = () => {
	nextTick(() => createSortable())
}

/** 重置编辑态（关闭面板时可选调用） */
const resetState = () => {
	editingKey.value = null
}

defineExpose({ onPopoverShow, resetState })

// ====== 生命周期 ======
onUnmounted(() => {
	destroySortables()
})
</script>

<style scoped>
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
