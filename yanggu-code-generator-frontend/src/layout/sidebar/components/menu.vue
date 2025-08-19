<template>
	<el-scrollbar>
		<el-menu
			:default-active="defaultActive"
			background-color="transparent"
			mode="vertical"
			:collapse-transition="false"
			router
			:collapse="appStore.isCollapseRef"
		>
			<menu-item v-for="menu in userStore.menuList" :key="menu.path" :menu="menu" :ref-map="menuRefs"></menu-item>
		</el-menu>
	</el-scrollbar>
</template>

<script setup lang="ts">
import MenuItem from '@/layout/sidebar/components/menu-item.vue'
import { useRoute } from 'vue-router'
import { computed, watch, nextTick, ref } from 'vue'
import { useAppStore } from '@/store/app-store'
import { useUserStore } from '@/store/user-store'

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()

// 存储所有菜单项引用的Map，格式为 <path, ref>
const menuRefs = ref<Map<string, any>>(new Map())

const defaultActive = computed(() => route.path)

// 监听路由变化，自动滚动
watch(
	() => route.path,
	newPath => {
		nextTick(() => {
			scrollToMenu(newPath)
		})
	},
	{ immediate: true }
)

// 激活的菜单滚动到可视区域中
const scrollToMenu = (path: string) => {
	// 查找当前激活菜单的 DOM
	const menuDom = menuRefs.value.get(path)?.$el

	if (!menuDom) {
		return
	}

	menuDom.scrollIntoView({
		behavior: 'smooth',
		block: 'nearest'
	})
}
</script>
