<template>
	<el-tooltip content="展开变量面板" placement="left">
		<div class="variable-edge" @click="emit('toggle')">
			<el-icon class="variable-edge-arrow"><ArrowLeft></ArrowLeft></el-icon>
			<span class="variable-edge-text">变量</span>
			<span v-if="count !== null" class="variable-edge-count">{{ count }}</span>
		</div>
	</el-tooltip>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'

defineOptions({
	name: 'VariableEdge'
})

// count 为 null 表示该组目录尚未就绪，此时不渲染徽标
defineProps<{
	count: number | null
}>()

const emit = defineEmits<{
	(e: 'toggle'): void
}>()
</script>

<style scoped>
/* 收起态的变量面板竖柄：规格 5.2 的 28px 右缘入口 */
.variable-edge {
	flex: none;
	width: 28px;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8px;
	cursor: pointer;
	border-left: 1px solid var(--el-border-color-lighter);
	background: var(--el-fill-color-lighter);
	color: var(--el-text-color-secondary);
	user-select: none;
}

.variable-edge:hover {
	background: var(--el-color-primary-light-9);
	color: var(--el-color-primary);
}

.variable-edge-arrow {
	font-size: 12px;
}

.variable-edge-text {
	writing-mode: vertical-lr;
	letter-spacing: 4px;
	font-size: 12px;
}

.variable-edge-count {
	min-width: 18px;
	padding: 0 5px;
	border-radius: 8px;
	background: var(--el-color-primary);
	color: #fff;
	font-size: 11px;
	line-height: 16px;
	text-align: center;
}
</style>
