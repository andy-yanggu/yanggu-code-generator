<template>
	<el-tooltip :content="title" :disabled="!isOverflow" :placement="placement">
		<el-text ref="textRef" class="text-tooltip-title" :style="{ maxWidth }">
			{{ title }}
		</el-text>
	</el-tooltip>
</template>

<script setup lang="ts">
import { defineProps, toRefs } from 'vue'
import { useTextOverflow } from '@/hooks/use-text-overflow'

defineOptions({
	name: 'TextTooltip'
})

const props = defineProps({
	title: {
		type: String,
		required: true
	},
	maxWidth: {
		type: String,
		default: '100px' // 如果是el-row和el-col的title，则maxWidth为100%
	},
	placement: {
		type: String,
		default: 'top'
	}
})
// 变成响应式数据
const { title: titleRef, maxWidth: maxWidthRef } = toRefs(props)

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
