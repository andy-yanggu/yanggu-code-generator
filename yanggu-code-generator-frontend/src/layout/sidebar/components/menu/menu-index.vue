<template>
	<el-scrollbar ref="scrollbarRef">
		<el-menu
			:default-active="userStore.activeMenuPath"
			mode="vertical"
			:unique-opened="systemSettingStore.menu.isOpenMenuUniqueOpened"
			:collapse-transition="systemSettingStore.menu.isOpenMenuCollapseAnimation"
			:collapse="appStore.isCollapse"
		>
			<menu-item v-for="menu in userStore.menuList" :key="menu.path" :menu="menu"></menu-item>
		</el-menu>
	</el-scrollbar>
</template>

<script setup lang="ts">
import MenuItem from '@/layout/sidebar/components/menu/menu-item.vue'
import { useRoute, useRouter } from 'vue-router'
import { nextTick, onMounted, ref, watch } from 'vue'
import { useAppStore, useSystemSettingStore, useUserStore } from '@/store'
import { findMenuByPath } from '@/store/user-store'
import type { ElScrollbar } from 'element-plus'
import { useElementVisibility } from '@vueuse/core'

defineOptions({
	name: 'MenuIndex'
})

const appStore = useAppStore()
const userStore = useUserStore()
const systemSettingStore = useSystemSettingStore()
const route = useRoute()
const router = useRouter()

// el-scrollbar 引用
const scrollbarRef = ref<InstanceType<typeof ElScrollbar>>()

// 监听路由变化，菜单自动滚动
watch(
	() => route.path,
	newPath => {
		if (userStore.activeMenuPath !== newPath) {
			userStore.setActiveMenuPath(newPath)
		}
		scrollToMenu(newPath)
	},
	{ immediate: false }
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

/**
 * 激活的菜单滚动到可视区域中
 * 使用 @vueuse/core 的 useElementVisibility 简化可视区域判断
 */
const scrollToMenu = (path: string) => {
	nextTick(() => {
		if (!scrollbarRef.value) {
			return
		}
		// 通过 ID 查找菜单项（格式为: menu-/path/converted-to-dashes）
		const menuId = `menu-${path.replace(/\//g, '-')}`
		const menuElement = document.getElementById(menuId)

		if (!menuElement) {
			console.warn(`[MenuScroll] 未找到菜单元素: ${menuId}`)
			return
		}

		// 使用 VueUse 的 useElementVisibility 判断是否在可视区域内
		const scrollbarWrap = scrollbarRef.value?.wrapRef
		if (!scrollbarWrap) {
			return
		}

		const isVisible = useElementVisibility(menuElement, { scrollTarget: scrollbarWrap })

		// 只有当菜单项不在可视区域内时才滚动
		if (!isVisible.value) {
			// 计算菜单项相对于滚动容器的位置
			const containerRect = scrollbarWrap.getBoundingClientRect()
			const menuRect = menuElement.getBoundingClientRect()
			const offsetTop = menuRect.top - containerRect.top + scrollbarWrap.scrollTop

			scrollbarRef.value?.scrollTo({
				top: offsetTop,
				behavior: 'smooth'
			})
		}
	})
}
</script>
