<template>
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
</template>

<script setup lang="ts">
import MenuItem from '@/layout/components/sidebar/components/menu-item.vue'
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { menuRoutes } from '@/router'
import { appStore } from '@/store'

const store = appStore()

const route = useRoute()
const defaultActive = computed(() => {
	const { path } = route
	return path
})
</script>
