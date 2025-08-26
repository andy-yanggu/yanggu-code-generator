<template>
	<div class="menu-item">
		<svg-icon v-if="icon" :icon="icon" class="svg-icon" />
		<el-tooltip :content="title" :disabled="!isTooltipEnabled" placement="top">
			<el-text ref="titleRef" truncated class="menu-title">
				{{ title }}
			</el-text>
		</el-tooltip>
	</div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, onUpdated, ref, computed } from 'vue'
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
const isCollapsed = ref(false) // 用于检测菜单折叠状态

// 检查文本是否溢出
const checkOverflow = () => {
	nextTick(() => {
		if (titleRef.value && titleRef.value.$el) {
			const element = titleRef.value.$el
			isTitleOverflow.value = element.scrollWidth > element.clientWidth
		}
	})
}

// 折叠时 tooltip 始终可用
const isTooltipEnabled = computed(() => {
	return isCollapsed.value || isTitleOverflow.value
})

// 监听窗口大小变化
let resizeObserver: ResizeObserver | null = null
onMounted(() => {
	checkOverflow()
	resizeObserver = new ResizeObserver(checkOverflow)
	if (titleRef.value && titleRef.value.$el) {
		resizeObserver.observe(titleRef.value.$el)
	}
})

onUpdated(() => {
	checkOverflow()
})

onUnmounted(() => {
	if (resizeObserver) {
		resizeObserver.disconnect()
	}
})
</script>

<style scoped>
.menu-item {
	display: flex;
	align-items: center; /* 图标+文字垂直居中 */
	overflow: hidden; /* 折叠时避免残留 */
}
.menu-title {
	flex: 1;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}
</style>
