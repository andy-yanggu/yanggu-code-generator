<template>
	<el-tooltip :content="title" :disabled="!isTitleOverflow" :placement="placement">
		<el-text ref="textRef" class="text-tooltip-title" :style="{ maxWidth }">
			{{ title }}
		</el-text>
	</el-tooltip>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import { useResizeObserver } from '@vueuse/core'

defineOptions({
	name: 'TextTooltipTitle'
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

const textRef = ref()
const isTitleOverflow = ref(false)

// 检查文本是否溢出
const checkOverflow = () => {
	nextTick(() => {
		if (textRef.value?.$el) {
			const element = textRef.value.$el
			isTitleOverflow.value = element.scrollWidth > element.offsetWidth
		}
	})
}

// 监听 title 和 maxWidth 的变化
watch(() => [props.title, props.maxWidth], checkOverflow)

onMounted(checkOverflow)

// 使用 VueUse 的 useResizeObserver 自动监听尺寸变化
useResizeObserver(textRef, checkOverflow)
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
