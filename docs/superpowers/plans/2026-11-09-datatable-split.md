# DataTable 列设置面板拆分 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 DataTable.vue（515 行）中的列设置面板提取为独立的 ColumnSettingsPanel.vue，使 DataTable 降至 ~310 行。

**Architecture:** ColumnSettingsPanel 作为纯展示组件接收 `v-model:columns` 双向绑定，内部持有 Sortable 拖拽、重命名、显隐、固定列的全部状态与逻辑；DataTable 保留 popover 容器壳，通过 `@show` 事件触发面板的 `onPopoverShow()` 方法。

**Tech Stack:** Vue 3 + TypeScript + Element Plus + Sortable.js + ColumnSettingItem.vue

**Spec:** `docs/superpowers/specs/2026-11-09-datatable-split-design.md`

## Global Constraints

- vue-tsc --noEmit 零新增错误
- ColumnSettingItem.vue 不做任何修改
- 列设置面板的外部行为完全不变
- DataTable 的 `defineExpose({ tableRef, cardRef })` 保持不变

---

### Task 1: 新建 ColumnSettingsPanel.vue

**Files:**
- Create: `yanggu-code-generator-frontend/src/components/crud/internal/ColumnSettingsPanel.vue`

**Interfaces:**
- Consumes: `TableFieldSchema`（类型）、`ColumnSettingItem`（子组件）、`Sortable`（拖拽库）
- Produces: 独立可用的列设置面板组件

- [ ] **Step 1: 创建组件骨架**

新建 `src/components/crud/internal/ColumnSettingsPanel.vue`，包含完整的 `<template>`、`<script setup>`、`<style scoped>` 三部分。

**template**：从 DataTable.vue L31-84 迁入列设置面板 UI，改为直接使用 `columns` defineModel：

```vue
<template>
	<div class="column-settings-panel">
		<div class="column-settings-header">
			<span class="column-settings-title">列设置</span>
			<el-button link type="primary" size="small" @click="handleReset">重置</el-button>
		</div>
		<div ref="columnListRef" class="column-list">
			<template v-for="col in columns" :key="col.key">
				<div v-if="col.children?.length" class="column-group" :class="{ 'is-fixed': col.fixed || col.disabled }">
					<column-setting-item
						:col="col"
						is-group
						:editing="editingKey === col.key"
						:editing-title="editingTitle"
						@update:editing-title="val => editingTitle = val"
						@toggle-visible="val => toggleColumnVisible(col, val)
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
							@-toggle-visible="val => toggleColumnVisible(child, val)"
							@start-rename="startRename(child)"
							@save-rename="saveRename()"
							@cancel-rename="cancelRename()"
						/>
					</div>
				</div>
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
```

**script**：从 DataTable.vue 迁入以下状态与函数：

```ts
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

// ====== 内部状态（从 DataTable 迁入） ======
const columnListRef = ref<HTMLElement>()
let sortableInstances: Sortable[] = []
const editingKey = ref<string | null>(null)
const editingTitle = ref('')

// ====== Sortable ======
const destroySortables = () => {
	sortableInstances.forEach(s => s.destroy())
	sortableInstances = []
}

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

	sortableInstances.push(
		new Sortable(el, {
			animation: 150,
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
const onPopoverShow = () => {
	nextTick(() => createSortable())
}

const resetState = () => {
	editingKey.value = null
}

defineExpose({ onPopoverShow, resetState })

// ====== 生命周期 ======
onUnmounted(() => {
	destroySortables()
})
```

**style**：从 DataTable.vue L476-513 迁入列设置面板相关样式：

```scss
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
```

- [ ] **Step 2: 确认新组件无类型错误**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit`
Expected: 零新增错误

---

### Task 2: DataTable.vue 删除已迁入代码并引用新组件

**Files:**
- Modify: `yanggu-code-generator-frontend/src/components/crud/DataTable.vue`

**Interfaces:**
- Consumes: `ColumnSettingsPanel`（Task 1 产出）
- Produces: DataTable 精简版（~310 行）

- [ ] **Step 1: 删除已迁入的状态与函数**

从 `<script setup>` 中删除以下代码：
- `columnListRef`（L253）
- `sortableInstances`（L255）
- `editingKey`（L258）
- `editingTitle`（L259）
- `destroySortables()`（L265-268）
- `reorder()`（L275-280）
- `createSortable()`（L282-334）
- `updateColumnByKey()`（L337-358）
- `toggleColumnVisible()`（L360-362）
- `toggleFixed()`（L364-366）
- `startRename()`（L373-376）
- `saveRename()`（L378-385）
- `cancelRename()`（L387-389）
- `onUnmounted` 中的 `destroySortables()` 调用（L442-444）

- [ ] **Step 2: 修改 handlePopoverShow**

将原来的 `handlePopoverShow` 改为通过 ref 调用 ColumnSettingsPanel 的 `onPopoverShow`：

```ts
const columnSettingsRef = ref<InstanceType<typeof ColumnSettingsPanel>>()

const handlePopoverShow = () => {
	columnSettingsRef.value?.onPopoverShow()
}
```

同时删除 `handlePopoverHide`（不再需要，Sortable 销毁由 ColumnSettingsPanel 的 onUnmounted 负责）。

- [ ] **Step 3: 修改 resetColumns**

```ts
const resetColumns = () => {
	columnSettingsRef.value?.resetState()
	emit('resetColumns')
}
```

- [ ] **Step 4: 替换模板中的列设置面板**

将 DataTable 模板中 `<el-popover>` 内部的面板内容（L31-84）替换为：

```vue
<column-settings-panel
	ref="columnSettingsRef"
	v-model:columns="columns"
	@reset="emit('resetColumns')"
/>
```

同时移除 `@hide="handlePopoverHide"` 事件绑定。

- [ ] **Step 5: 导入新组件**

在 `<script setup>` 的 import 区域新增：

```ts
import ColumnSettingsPanel from './internal/ColumnSettingsPanel.vue'
```

- [ ] **Step 6: 删除已迁入的样式**

从 `<style scoped>` 中删除以下样式规则（已迁入 ColumnSettingsPanel）：
- `.column-settings-trigger` — 保留（仍在 DataTable 工具栏中使用）
- `.column-settings-panel` 及其子规则 — 删除
- `.column-list` — 删除
- `.column-group` — 删除
- `.column-group-children` — 删除

- [ ] **Step 7: 确认类型无报错**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit`
Expected: 零新增错误

---

### Task 3: 浏览器 MCP 自测

**Files:** 无新增改动

- [ ] **Step 1: 启动前端 dev server**

Run: `cd yanggu-code-generator-frontend && pnpm dev`

- [ ] **Step 2: 验证列设置面板功能**

通过浏览器 MCP 打开任意列表页面（如枚举管理），验证：
1. 点击"列设置"按钮，面板正常弹出
2. 拖拽列设置面板中的列，排序正常
3. 勾选/取消勾选，列显隐正常
4. 点击固定左/右按钮，列固定正常
5. 点击重命名按钮，编辑态切换正常，保存/取消正常
6. 点击"重置"按钮，面板状态重置正常
7. 关闭面板再打开，状态正确恢复

- [ ] **Step 3: 确认 DataTable 其他功能不受影响**

1. 表格数据正常渲染
2. 多级表头正常
3. 工具栏搜索/刷新/最大化正常
4. 分页正常

---

### Task 4: vue-tsc 基线差分验证

**Files:** 无新增改动

- [ ] **Step 1: 全量 vue-tsc 检查**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit`
Expected: 错误数 ≤ 既有基线，新增/修改文件零错误
