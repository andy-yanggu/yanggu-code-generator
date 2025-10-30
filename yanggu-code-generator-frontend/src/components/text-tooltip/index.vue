<template>
	<el-tooltip :content="title" :disabled="!isTitleOverflow" :placement="placement">
		<el-text ref="textRef" class="text-tooltip-title" :style="{ maxWidth }">
			{{ title }}
		</el-text>
	</el-tooltip>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

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

let observer: ResizeObserver | null = null

// 检查文本是否溢出
const checkOverflow = () => {
	nextTick(() => {
		if (textRef.value?.$el) {
			const element = textRef.value.$el
			isTitleOverflow.value = element.scrollWidth > element.offsetWidth
		}
	})
}

// watch title的值是否发生变化，如果发生变化则重新检查
watch(
	() => props.title,
	() => {
		checkOverflow()
	}
)

// maxWidth 变化时也重新检测（支持响应式传入 maxWidth）
watch(
	() => props.maxWidth,
	() => {
		checkOverflow()
	}
)

onMounted(() => {
	nextTick(() => {
		const el = textRef.value?.$el
		if (!el) {
			return
		}

		checkOverflow()

		// ✅ 使用 ResizeObserver 自动监听元素尺寸变化
		observer = new ResizeObserver(checkOverflow)
		observer.observe(el)
	})
})

onBeforeUnmount(() => {
	if (observer && textRef.value?.$el) {
		observer.unobserve(textRef.value.$el)
	}
})
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
