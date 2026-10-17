<!--
	ColumnSettingItem：列设置面板中的「一行」（纯展示组件）
	- 复用于三种位置：顶层叶子列、分组表头行、组内叶子列
	- 支持显示态（拖拽手柄 + 显隐勾选 + 固定左右 + 重命名）与编辑态（改名输入）
	- 所有状态与变更逻辑由父组件（DataTable）持有，本组件只负责渲染 + emit
-->
<template>
	<div class="column-list-item" :class="{ 'is-fixed': col.fixed || col.disabled, 'is-disabled': col.disabled, 'is-editing': editing, 'is-child': indent, 'is-group': isGroup }">
		<!-- 编辑模式 -->
		<template v-if="editing">
			<span class="drag-handle"
				><el-icon :size="14"><Lock /></el-icon
			></span>
			<el-input
				:model-value="editingTitle"
				size="small"
				placeholder="请输入列名"
				class="rename-input"
				@click.stop
				@update:model-value="(v: string) => emit('update:editingTitle', v)"
				@keydown.enter="emit('saveRename')"
				@keydown.escape="emit('cancelRename')"
			/>
			<div class="rename-btns">
				<el-button type="primary" :icon="Check" link size="small" @click.stop="emit('saveRename')" />
				<el-button :icon="Close" link size="small" @click.stop="emit('cancelRename')" />
			</div>
		</template>
		<!-- 显示模式 -->
		<template v-else>
			<span class="drag-handle">
				<el-icon v-if="col.fixed || col.disabled" :size="14"><Lock /></el-icon>
				<el-icon v-else :size="14"><Rank /></el-icon>
			</span>
			<el-checkbox :model-value="col.visible !== false" :disabled="col.disabled" @change="(val: boolean | string | number) => emit('toggleVisible', val)">
				<span :class="{ 'group-label': isGroup }">{{ col.label }}</span>
			</el-checkbox>
			<template v-if="!col.disabled && showFixed">
				<button class="pin-btn" :class="{ 'is-active': col.fixed === 'left' }" title="固定在左边" @click="emit('toggleFixed', 'left')">
					<el-icon :size="14"><ArrowLeft /></el-icon>
				</button>
				<button class="pin-btn" :class="{ 'is-active': col.fixed === 'right' }" title="固定在右边" @click="emit('toggleFixed', 'right')">
					<el-icon :size="14"><ArrowRight /></el-icon>
				</button>
			</template>
			<el-button v-if="!col.disabled" :icon="Edit" type="primary" link size="small" class="rename-btn" title="重命名" @click.stop="emit('startRename')" />
		</template>
	</div>
</template>

<script setup lang="ts">
import { ArrowLeft, ArrowRight, Check, Close, Edit, Lock, Rank } from '@element-plus/icons-vue'
import type { TableColumnSchema } from '@/types/schema'

defineOptions({
	name: 'ColumnSettingItem'
})

withDefaults(
	defineProps<{
		/** 列配置 */
		col: TableColumnSchema
		/** 是否处于改名编辑态 */
		editing?: boolean
		/** 编辑态输入框的值 */
		editingTitle?: string
		/** 是否显示固定左右按钮（组内叶子不显示） */
		showFixed?: boolean
		/** 是否为分组表头行 */
		isGroup?: boolean
		/** 是否缩进（组内叶子） */
		indent?: boolean
	}>(),
	{
		editing: false,
		editingTitle: '',
		showFixed: true,
		isGroup: false,
		indent: false
	}
)

const emit = defineEmits<{
	(e: 'toggleVisible', val: boolean | string | number): void
	(e: 'toggleFixed', dir: 'left' | 'right'): void
	(e: 'startRename'): void
	(e: 'saveRename'): void
	(e: 'cancelRename'): void
	(e: 'update:editingTitle', val: string): void
}>()
</script>

<style scoped>
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
/* 组内叶子缩进 */
.column-list-item.is-child {
	padding-left: 24px;
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
.group-label {
	font-weight: 600;
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
