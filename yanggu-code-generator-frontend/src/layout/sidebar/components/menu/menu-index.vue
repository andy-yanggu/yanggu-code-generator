<template>
	<el-scrollbar>
		<el-menu
			:default-active="activePath"
			background-color="transparent"
			mode="vertical"
			router
			:collapse-transition="false"
			:collapse="appStore.isCollapseRef"
		>
			<menu-item v-for="menu in userStore.menuList" :key="menu.path" :menu="menu" :ref-map="menuRefs" @update:active-path="setActivePath"></menu-item>
		</el-menu>
	</el-scrollbar>
</template>

<script setup lang="ts">
import MenuItem from '@/layout/sidebar/components/menu/menu-item.vue'
import { useRoute } from 'vue-router'
import { watch, nextTick, ref } from 'vue'
import { useAppStore } from '@/store/app-store'
import { useUserStore } from '@/store/user-store'

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()

// 存储所有菜单项引用的Map，格式为 <path, ref>
const menuRefs = ref<Map<string, any>>(new Map())

const activePath = ref(route.path)

// 监听路由变化，菜单自动滚动
watch(
	() => route.path,
	newPath => {
		nextTick(() => {
			activePath.value = newPath
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

const setActivePath = (path: string) => {
	activePath.value = path
}
</script>
