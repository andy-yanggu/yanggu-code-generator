<template>
	<el-tooltip :content="content" :disabled="!isOverflow" :placement="placement">
		<el-text ref="textRef" :class="['text-tooltip-title', textClass]" :style="{ maxWidth }">
			{{ title }}
		</el-text>
	</el-tooltip>
</template>

<script setup lang="ts">
import { computed, defineProps, toRefs } from 'vue'
import { useTextOverflow } from '@/hooks/use-text-overflow'

defineOptions({
	name: 'TextTooltip'
})

const props = defineProps({
	title: {
		type: String,
		required: true
	},
	tooltipContent: {
		type: String,
		default: ''
	},
	maxWidth: {
		type: String,
		default: '100px' // 如果是el-row和el-col的title，则maxWidth为100%
	},
	placement: {
		type: String,
		default: 'top'
	},
	textClass: {
		type: String,
		default: ''
	}
})
// 变成响应式数据
const { title: titleRef, maxWidth: maxWidthRef } = toRefs(props)

const content = computed(() => {
	return props.tooltipContent || props.title
})

const { textRef, isOverflow } = useTextOverflow(titleRef, maxWidthRef)
</script>
<style scoped lang="scss">
.text-tooltip-title {
	display: inline-block;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	vertical-align: middle;
	color: inherit;
}
</style>
