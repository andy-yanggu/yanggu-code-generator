<template>
	<el-scrollbar>
		<el-menu
			:default-active="defaultActive"
			background-color="transparent"
			mode="vertical"
			:collapse-transition="false"
			router
			:collapse="store.isCollapseRef"
		>
			<menu-item v-for="menu in menuRoutes" :key="menu.path" :menu="menu"></menu-item>
		</el-menu>
	</el-scrollbar>
</template>

<script setup lang="ts">
import MenuItem from '@/layout/components/sidebar/components/menu-item.vue'
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { menuRoutes } from '@/router'
import { useAppStore } from '@/store/use-app-store'

const store = useAppStore()

const route = useRoute()
const defaultActive = computed(() => {
	const { path } = route
	return path
})
</script>
