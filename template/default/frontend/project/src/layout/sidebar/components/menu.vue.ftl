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
			<menu-item v-for="menu in userStore.menuList" :key="menu.path" :menu="menu"></menu-item>
		</el-menu>
	</el-scrollbar>
</template>

<script setup lang="ts">
import MenuItem from '@/layout/sidebar/components/menu-item.vue'
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { useAppStore } from '@/store/app-store'
import { useUserStore } from '@/store/user-store'

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()
const defaultActive = computed(() => {
	const { path } = route
	return path
})
</script>
