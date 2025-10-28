<template>
	<div class="menu-item">
		<svg-icon v-if="icon" :icon="icon" is-pointer></svg-icon>
		<el-tooltip :content="title" :disabled="!isTitleOverflow" placement="top">
			<el-text ref="titleRef" class="menu-title" :style="{ maxWidth: systemSettingStore.menuExpandWidth - 100 + 'px' }">
				{{ title }}
			</el-text>
		</el-tooltip>
	</div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, onUpdated, ref } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import { ElText } from 'element-plus'
import { useSystemSettingStore } from '@/store/system-setting-store'

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

const titleRef = ref()
const isTitleOverflow = ref(false)
const systemSettingStore = useSystemSettingStore()

// 检查文本是否溢出
const checkOverflow = () => {
	nextTick(() => {
		if (titleRef.value && titleRef.value.$el) {
			const element = titleRef.value.$el
			isTitleOverflow.value = element.scrollWidth > element.offsetWidth
		}
	})
}

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
}
.menu-title {
	display: inline-block;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	vertical-align: middle;
	color: inherit;
}
</style>
