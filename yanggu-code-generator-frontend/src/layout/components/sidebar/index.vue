<template>
	<div style="margin-bottom: 15px">
		<el-avatar
			src="./favicon.ico"
			size="small"
			style="width: 25px; height: 25px; position: relative; top: 5px; margin-left: 18px; margin-right: 5px"
		></el-avatar>
		<el-text v-if="!store.isCollapseRef" tag="b">{{ appTitle }}</el-text>
	</div>
	<Menu></Menu>
</template>

<script setup lang="ts">
import Menu from '@/layout/components/sidebar/components/menu.vue'
import { useAppStore } from '@/store/use-app-store'
const appTitle = import.meta.env.VITE_APP_TITLE

const store = useAppStore()
</script>
