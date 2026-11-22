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
			<el-main ref="layoutMainRef" class="layout-main">
				<layout-main></layout-main>
			</el-main>
		</el-container>
	</el-container>
</template>

<script setup lang="ts">
import LayoutSidebar from '@/layout/sidebar/index.vue'
import LayoutNavbar from '@/layout/navbar/index.vue'
import LayoutMain from '@/layout/main/index.vue'
import { useAppStore, useSystemSettingStore } from '@/store'
import { computed } from 'vue'
import { storeToRefs } from 'pinia'

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
</script>
