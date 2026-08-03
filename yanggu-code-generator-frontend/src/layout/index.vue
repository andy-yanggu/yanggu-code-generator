<template>
	<el-container class="layout-container">
		<!-- 侧边栏 -->
		<el-aside class="layout-sidebar" :style="{ width: calculateLayoutSidebarWidth }">
			<layout-sidebar></layout-sidebar>
		</el-aside>
		<el-container direction="vertical">
			<!-- 顶栏 -->
			<el-header class="layout-header">
				<layout-navbar></layout-navbar>
			</el-header>
			<!-- 内容 -->
			<el-main ref="layoutMainRef" class="layout-main" :class="{ 'layout-fullscreen': appStore.isFullscreen }">
				<layout-main></layout-main>
				<!-- 全屏退出按钮 -->
				<div v-if="appStore.isFullscreen" class="layout-fullscreen-exit" @click="appStore.toolFullscreen()">
					<el-tooltip content="退出全屏 (Esc)" placement="left">
						<svg-icon icon="icon-fullscreen-exit" size="18px"></svg-icon>
					</el-tooltip>
				</div>
			</el-main>
		</el-container>
	</el-container>
</template>

<script setup lang="ts">
import LayoutSidebar from '@/layout/sidebar/index.vue'
import LayoutNavbar from '@/layout/navbar/index.vue'
import LayoutMain from '@/layout/main/index.vue'
import { useAppStore, useSystemSettingStore } from '@/store'
import { computed, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'

import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'Layout'
})

const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()
const { layoutMainRef } = storeToRefs(appStore)

// 计算布局宽度
const calculateLayoutSidebarWidth = computed(() => {
	return (appStore.isCollapse ? systemSettingStore.menu.menuFoldWidth : systemSettingStore.menu.menuExpandWidth) + 'px'
})

// Esc 键退出全屏
const onKeydown = (e: KeyboardEvent) => {
	if (e.key === 'Escape' && appStore.isFullscreen) {
		appStore.toolFullscreen()
	}
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
/* 内容区最大化：fixed 覆盖整个视口，自然遮盖侧边栏和顶栏 */
/* z-index 需低于 Element Plus 弹出层默认值(2000)，确保 teleported 的 popover/dropdown 可见 */
.layout-main.layout-fullscreen {
	position: fixed;
	inset: 0;
	z-index: 100;
}

/* 全屏退出按钮：悬浮在右上角 */
.layout-fullscreen-exit {
	position: fixed;
	top: 12px;
	right: 12px;
	z-index: 101;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 32px;
	height: 32px;
	border-radius: 6px;
	background: var(--el-bg-color-overlay, #fff);
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
	cursor: pointer;
	transition: background 0.2s;
}

.layout-fullscreen-exit:hover {
	background: var(--el-fill-color-light, #f5f7fa);
}
</style>
