<template>
	<el-scrollbar>
		<el-menu
			:default-active="userStore.activeMenuPath"
			background-color="transparent"
			mode="vertical"
			:unique-opened="systemSettingStore.menu.isOpenMenuUniqueOpened"
			:collapse-transition="systemSettingStore.menu.isOpenMenuCollapseAnimation"
			:collapse="appStore.isCollapse"
		>
			<menu-item v-for="menu in userStore.menuList" :key="menu.path" :menu="menu" :ref-map="menuRefs"></menu-item>
		</el-menu>
	</el-scrollbar>
</template>

<script setup lang="ts">
import MenuItem from '@/layout/sidebar/components/menu/menu-item.vue'
import { useRoute, useRouter } from 'vue-router'
import { nextTick, onMounted, ref, watch } from 'vue'
import { useAppStore, useSystemSettingStore, useUserStore } from '@/store'
import { findMenuByPath } from '@/store/user-store'

defineOptions({
	name: 'MenuIndex'
})

const appStore = useAppStore()
const userStore = useUserStore()
const systemSettingStore = useSystemSettingStore()
const route = useRoute()
const router = useRouter()

// 存储所有菜单项引用的Map，格式为 <path, ref>
const menuRefs = ref<Map<string, any>>(new Map())

// 监听路由变化，菜单自动滚动
watch(
	() => route.path,
	newPath => {
		nextTick(() => {
			userStore.setActiveMenuPath(newPath)
			scrollToMenu(newPath)
		})
	},
	{ immediate: true }
)

// 初始化激活菜单
onMounted(() => {
	const activeMenuPath = userStore.activeMenuPath
	const findMenu = findMenuByPath(userStore.menuList, activeMenuPath)
	// 外链不处理
	if (findMenu?.meta?.type === 4) {
		return
	}
	if (activeMenuPath !== route.fullPath) {
		router.push(activeMenuPath)
	}
})

// 激活的菜单滚动到可视区域中
const scrollToMenu = (path: string) => {
	// 查找当前激活菜单的 DOM
	// console.log('menuRefs.value', menuRefs.value)
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
