<template>
	<div class="logo-container">
		<el-avatar src="./favicon.ico" size="small" class="logo-avatar"></el-avatar>
		<el-text v-if="!store.isCollapseRef" tag="b">{{ appTitle }}</el-text>
	</div>
</template>
<script setup lang="ts">
import { useAppStore } from '@/store/app-store'

const appTitle = import.meta.env.VITE_APP_TITLE
const store = useAppStore()
</script>

<style scoped>
.logo-container {
	margin-left: 11px;
	display: flex;
	align-items: center;
	padding: 8px;
}

.logo-avatar {
	margin-right: 9px;
	color: #2c3e50;
	font-size: 16px;
	font-weight: 550;
}
</style>
