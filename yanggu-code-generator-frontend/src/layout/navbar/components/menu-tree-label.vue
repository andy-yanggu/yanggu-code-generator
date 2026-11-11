<template>
	<el-tooltip :content="title" :disabled="!isOverflow" placement="top">
		<span ref="textRef" class="menu-tree-label">
			<span v-for="(segment, index) in segments" :key="index" :class="{ 'label-hit': segment.hit }">{{ segment.text }}</span>
		</span>
	</el-tooltip>
</template>

<script setup lang="ts">
import { useTextOverflow } from '@/hooks/use-text-overflow'

defineOptions({
	name: 'MenuTreeLabel'
})

interface Segment {
	text: string
	hit: boolean
}

const props = defineProps<{
	/** 节点完整文本，同时用作 tooltip 内容 */
	title: string
	/** 搜索关键词，命中的片段高亮显示；空关键词时整段按普通文本渲染 */
	keyword: string
}>()

/**
 * 命中片段用多个 span 分段渲染，而不是把 title 拼成 HTML 交给 v-html：
 * 菜单名来自路由配置与后端接口，拼 HTML 字符串会把注入风险引进弹窗。
 */
const segments = computed<Segment[]>(() => {
	const keyword = props.keyword.trim().toLowerCase()
	const title = props.title
	if (!keyword) {
		return [{ text: title, hit: false }]
	}
	const result: Segment[] = []
	const lowerTitle = title.toLowerCase()
	let cursor = 0
	let index = lowerTitle.indexOf(keyword)
	while (index > -1) {
		if (index > cursor) {
			result.push({ text: title.slice(cursor, index), hit: false })
		}
		result.push({ text: title.slice(index, index + keyword.length), hit: true })
		cursor = index + keyword.length
		index = lowerTitle.indexOf(keyword, cursor)
	}
	if (cursor < title.length) {
		result.push({ text: title.slice(cursor), hit: false })
	}
	return result
})

// 沿用 text-tooltip 的约定：只有文字被省略号截断时才出 tooltip，故此处要检测溢出
const { textRef, isOverflow } = useTextOverflow(toRef(props, 'title'))
</script>

<style scoped>
/* flex: 1 + min-width: 0 让本组件作为 .menu-tree-node 的弹性项，剩余宽度不足时才截断 */
.menu-tree-label {
	display: block;
	flex: 1;
	min-width: 0;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	color: inherit;
}
.label-hit {
	color: var(--el-color-primary);
	font-weight: 600;
}
</style>
