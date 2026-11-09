# DataTable 列设置面板拆分设计

## 概述

将 `DataTable.vue`（515 行）中的列设置面板逻辑提取为独立的 `ColumnSettingsPanel.vue` 组件，使 DataTable 降至 ~310 行，提升可维护性与职责清晰度。

## 现状

DataTable.vue 当前承载四大职责：

| 职责 | 行数范围 | 约行数 | 说明 |
|------|---------|--------|------|
| 工具栏 | L1-91 (模板) + L219-233 (逻辑) | ~85 | 搜索开关、刷新、列设置触发、最大化 |
| 表格渲染 | L93-141 (模板) + L235-249, L391-434 (逻辑) | ~100 | 多级表头、插槽转发、visibleColumns、toColumnProps |
| 列设置面板 | L17-85 (模板) + L251-389 (逻辑) + L476-513 (样式) | ~210 | Sortable 拖拽、重命名、显隐、固定列、重置 |
| 分页 | L143-154 (模板) | ~15 | el-pagination |

核心问题：列设置面板独占 ~210 行（约 40%），且内部状态高度内聚（`columnSettingsVisible` / `editingKey` / `editingTitle` / `sortableInstances` / `columnListRef`），与表格渲染几乎无耦合。

## 设计决策

### 决策 1：只拆列设置面板，不拆工具栏

| 候选拆分组 | 评估 |
|-----------|------|
| TableToolbar.vue（工具栏） | 工具栏只有 4 个按钮 + 1 个 popover 触发器，拆出后仍需通过 DataTable 的 v-model 双向绑定 showSearch/maximized，新增一层 props 透传但没有减少逻辑，纯粹搬家 |
| ColumnSettingsPanel.vue（列设置面板） | 210 行，内部持有 5 个 ref + 2 个 Sortable 实例 + 6 个事件处理函数，与 DataTable 的唯一耦合点是 `columns` v-model。提取后 DataTable 只需传 `v-model:columns` + 监听 `@reset` |

**结论**：仅提取 ColumnSettingsPanel.vue。

### 决策 2：popover 容器归属

`el-popover` 的 `#reference` 在工具栏中，而面板内容在 ColumnSettingsPanel 中。两种方案：

- A：popover 留在 DataTable，ColumnSettingsPanel 只作为面板内容 → popover 的 `@show`/`@hide` 事件由 DataTable 转发给 ColumnSettingsPanel
- B：整个 popover 移入 ColumnSettingsPanel，DataTable 只放一个触发按钮 → ColumnSettingsPanel 需要接收 popover 的 placement/width/popper-class 等 UI 参数

**选择 A**：理由：
1. ColumnSettingsPanel 保持纯粹的"面板内容"语义，不感知 popover 容器
2. popover 的 UI 参数（placement/width/popper-class）属于布局层，不应下沉到面板组件
3. 代价仅为 DataTable 保留 `columnSettingsVisible` 状态 + `handlePopoverShow` 转发

### 决策 3：columns 双向绑定方式

ColumnSettingsPanel 需要修改 columns 数组（拖拽重排、显隐切换、固定列、重命名）。使用 Vue 3 的 `v-model:columns` 双向绑定：

```typescript
// ColumnSettingsPanel.vue
const columns = defineModel<TableFieldSchema[]>('columns', { required: true })
```

面板内部所有修改直接写 `columns.value = ...`，DataTable 侧自动同步。

## 接口设计

### ColumnSettingsPanel.vue

**Props（v-model）：**
- `columns: TableFieldSchema[]` — 列配置数组（双向绑定）

**Emits：**
- `update:columns` — v-model 双向绑定自动触发
- `reset` — 用户点击"重置"按钮，向上冒泡

**内部状态（全部从 DataTable 迁入）：**

| 状态 | 原 DataTable 变量名 | 说明 |
|------|-------------------|------|
| `columnListRef` | `ref<HTMLElement>()` | 列列表 DOM 引用（Sortable 挂载点） |
| `sortableInstances` | `Sortable[]` | Sortable 实例数组（根 + 分组） |
| `editingKey` | `ref<string \| null>(null)` | 正在重命名的列 key |
| `editingTitle` | `ref('')` | 重命名输入框的值 |

**内部函数（全部从 DataTable 迁入）：**

| 函数 | 说明 |
|------|------|
| `createSortable()` | 创建根 Sortable + 分组子 Sortable |
| `destroySortables()` | 销毁所有 Sortable 实例 |
| `reorder(arr, oldIndex, newIndex)` | 通用数组重排 |
| `updateColumnByKey(list, key, patch)` | 递归更新指定 key 的列（不可变） |
| `toggleColumnVisible(col, visible)` | 切换列显隐 |
| `toggleFixed(col, direction)` | 切换列固定方向 |
| `startRename(col)` | 进入重命名态 |
| `saveRename()` | 保存重命名 |
| `cancelRename()` | 取消重命名 |

**暴露方法（供 DataTable 调用）：**
- `onPopoverShow()` — popover 展示后调用，内部触发 `createSortable()`
- `resetState()` — 重置编辑态（editingKey = null）

### DataTable.vue 改动

**保留的状态：**
- `columnSettingsVisible` — popover 可见性（决策 2 的代价）
- `handlePopoverShow()` — 转发给 ColumnSettingsPanel.onPopoverShow()
- `handlePopoverHide()` — 不再需要，Sortable 销毁由 ColumnSettingsPanel 的 `onUnmounted` 负责

**删除的状态/函数：**
- `columnListRef`、`sortableInstances`、`editingKey`、`editingTitle`
- `destroySortables()`、`reorder()`、`createSortable()`
- `updateColumnByKey()`、`toggleColumnVisible()`、`toggleFixed()`
- `startRename()`、`saveRename()`、`cancelRename()`

**删除的模板：**
- 列设置面板内部 UI（L31-84 的 `.column-settings-panel` 整块）

**删除的样式：**
- 列设置面板相关 CSS（L476-513 中 `.column-settings-*` / `.column-list*` / `.column-group*`）

**新增引用：**
```vue
<column-settings-panel
  v-model:columns="columns"
  ref="columnSettingsRef"
  @reset="emit('resetColumns')"
/>
```

## 迁入清单

| 项目 | DataTable 位置 | → ColumnSettingsPanel |
|------|---------------|----------------------|
| `columnListRef` | L253 | 内部 ref |
| `sortableInstances` | L255 | 内部 let |
| `editingKey` | L258 | 内部 ref |
| `editingTitle` | L259 | 内部 ref |
| `destroySortables()` | L265-268 | 内部函数 |
| `reorder()` | L275-280 | 内部函数 |
| `createSortable()` | L282-334 | 内部函数（`onPopoverShow` 调用） |
| `updateColumnByKey()` | L337-358 | 内部函数 |
| `toggleColumnVisible()` | L360-362 | 内部函数 |
| `toggleFixed()` | L364-366 | 内部函数 |
| `startRename()` | L373-376 | 内部函数 |
| `saveRename()` | L378-385 | 内部函数 |
| `cancelRename()` | L387-389 | 内部函数 |
| 面板模板 | L31-84 | `<template>` 主体 |
| 面板样式 | L476-513 | `<style scoped>` |
| `onUnmounted(destroySortables)` | L442-444 | `onUnmounted(destroySortables)` |

## 模板对照

### 迁入 ColumnSettingsPanel 的模板

```vue
<template>
	<div class="column-settings-panel">
		<div class="column-settings-header">
			<span class="column-settings-title">列设置</span>
			<el-button link type="primary" size="small" @click="handleReset">重置</el-button>
		</div>
		<div ref="columnListRef" class="column-list">
			<template v-for="col in columns" :key="col.key">
				<!-- 分组：表头行 + 组内叶子容器 -->
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
							@-toggle-visible="val => toggleColumnVisible(child, val)"
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
```

### DataTable 中保留的 popover 壳

```vue
<el-popover
	v-model:visible="columnSettingsVisible"
	placement="top"
	:width="300"
	trigger="click"
	popper-class="yanggu-column-settings-popover"
	@show="handlePopoverShow"
>
	<template #reference>
		<span class="column-settings-trigger">
			<icon-button size="16px" :el-icon="Setting" tooltip="列设置" />
		</span>
	</template>
	<column-settings-panel
		ref="columnSettingsRef"
		v-model:columns="columns"
		@reset="emit('resetColumns')"
	/>
</el-popover>
```

## 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| Sortable 实例必须在 popover 渲染后创建 | 中 | DataTable `@show` → `columnSettingsRef.onPopoverShow()` → `nextTick(createSortable())` |
| columns v-model 双向绑定的响应性传递 | 低 | Vue 3 `defineModel` 天然支持，DataTable 的 `columns` defineModel 与 ColumnSettingsPanel 的 `columns` defineModel 通过 `v-model:columns` 串联 |
| 列设置面板样式作用域变更 | 低 | 样式随组件迁移到 ColumnSettingsPanel 的 `<style scoped>`，popover 容器外的 `popper-class` 样式不受 scoped 影响（已有 `:deep` 处理） |
| `onUnmounted` 中 `destroySortables` 的生命周期 | 低 | 迁移到 ColumnSettingsPanel 的 `onUnmounted`，DataTable 不再需要 |
| ColumnSettingItem.vue 不受影响 | 无 | ColumnSettingItem 的 props/emits 接口完全不变 |

## 约束

- vue-tsc --noEmit 零新增错误
- 列设置面板的外部行为完全不变（拖拽排序、重命名、显隐、固定、重置）
- ColumnSettingItem.vue 不做任何修改
- DataTable 的 `defineExpose({ tableRef, cardRef })` 保持不变
- 不引入新依赖

## 改动文件清单

| 文件 | 操作 | 行数变化 |
|------|------|---------|
| `src/components/crud/internal/ColumnSettingsPanel.vue` | **新建** | +200 行 |
| `src/components/crud/DataTable.vue` | 修改 | 515 → ~310 行 |
