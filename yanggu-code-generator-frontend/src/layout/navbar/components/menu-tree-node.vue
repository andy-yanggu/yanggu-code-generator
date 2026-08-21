<template>
	<div class="tree-node" @click.stop="handleClick">
		<span class="tree-prefix" :style="{ marginLeft: level * 16 + 'px' }">{{ hasChildren ? '├─' : '│' }}</span>
		<icon-text-tooltip :icon="node.icon ?? ''" :title="node.title" :max-width="'300px'"></icon-text-tooltip>
	</div>

	<div v-if="node.children && node.children.length" class="tree-children">
		<MenuTreeNode v-for="child in node.children" :key="child.title" :node="child" :level="level + 1" :on-select="onSelect"></MenuTreeNode>
	</div>
</template>
<script setup lang="ts">
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'

defineOptions({
	name: 'MenuTreeNode'
})

export interface TreeNode {
	title: string
	icon?: string
	path?: string
	type: number
	externalUrl?: string
	children?: TreeNode[]
}

const props = defineProps<{
	node: TreeNode
	level: number
	onSelect: (node: TreeNode) => void
}>()

const handleClick = (_: Event) => {
	if (!props.node.children || props.node.children.length === 0) {
		props.onSelect(props.node)
	}
}
const hasChildren = computed(() => !!(props.node.children && props.node.children.length))
</script>
<style scoped>
/* 树节点样式 */
.tree-node {
	display: flex;
	align-items: center;
	gap: 6px;
	padding: 5px 5px 10px 10px;
	cursor: pointer;
}
.tree-node:hover {
	background-color: var(--el-color-primary-light-9);
	color: var(--el-color-primary);
}
.tree-prefix {
	font-family: monospace;
	white-space: pre;
	flex-shrink: 0;
}
.tree-children {
	margin-left: 16px;
}
</style>
