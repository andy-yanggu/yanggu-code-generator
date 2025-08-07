<template>
	<el-sub-menu v-if="menu.meta.type === 0" :key="'sub-menu-' + menu.path" :index="menu.path">
		<template #title>
			<svg-icon :icon="menu.meta.icon"></svg-icon>
			<el-tooltip :content="menu.meta.title" :disabled="!isTitleOverflow" placement="top">
				<el-text ref="titleRef" truncated>{{ menu.meta.title }}</el-text>
			</el-tooltip>
		</template>
		<!-- 递归渲染目录或者菜单 -->
		<template v-if="menu.children && menu.children.length > 0">
			<menu-item v-for="sub in menu.children" :key="'sub-menu-' + sub.path" :menu="sub"></menu-item>
		</template>
	</el-sub-menu>
	<el-menu-item v-else-if="menu.meta.type === 1 && !menu.meta.hidden" :key="'menu-item-' + menu.path" :index="menu.path">
		<svg-icon :icon="menu.meta.icon"></svg-icon>
		<el-tooltip :content="menu.meta.title" :disabled="!isTitleOverflow" placement="top">
			<el-text ref="titleRef" truncated>{{ menu.meta.title }}</el-text>
		</el-tooltip>
	</el-menu-item>
</template>

<script setup lang="ts">
import { ref, onMounted, onUpdated, nextTick, onUnmounted } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import { ElText } from 'element-plus'

defineProps({
	menu: {
		type: Object,
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
