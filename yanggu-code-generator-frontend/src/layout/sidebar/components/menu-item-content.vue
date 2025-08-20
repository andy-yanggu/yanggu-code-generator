<template>
	<svg-icon v-if="icon" :icon="icon"></svg-icon>
	<el-tooltip :content="title" :disabled="!isTitleOverflow" placement="top">
		<el-text ref="titleRef" truncated>{{ title }}</el-text>
	</el-tooltip>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, onUpdated, ref } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import { ElText } from 'element-plus'

defineProps({
	icon: {
		type: String,
		default: ''
	},
	title: {
		type: String,
		required: true
	}
})
const titleRef = ref<InstanceType<typeof ElText> | null>(null)
const isTitleOverflow = ref(false)
// 检查文本是否溢出
const checkOverflow = () => {
	nextTick(() => {
		if (titleRef.value && titleRef.value.$el) {
			const element = titleRef.value.$el
			isTitleOverflow.value = element.scrollWidth > element.clientWidth
		}
	})
}

// 在组件挂载和更新时检查溢出
onMounted(() => {
	checkOverflow()
})

onUpdated(() => {
	checkOverflow()
})

// 监听窗口大小变化
let resizeObserver: ResizeObserver | null = null
onMounted(() => {
	resizeObserver = new ResizeObserver(checkOverflow)
	if (titleRef.value && titleRef.value.$el) {
		resizeObserver.observe(titleRef.value.$el)
	}
})

onUnmounted(() => {
	if (resizeObserver) {
		resizeObserver.disconnect()
	}
})
</script>
