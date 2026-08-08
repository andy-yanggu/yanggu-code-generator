<template>
	<div class="logo-container" @click="clickHandler()">
		<el-avatar src="./favicon.ico" size="small" class="logo-avatar"></el-avatar>
		<el-text v-if="!appStore.isCollapse" tag="b">{{ appTitle }}</el-text>
	</div>
</template>
<script setup lang="ts">
import { useAppStore, useSystemSettingStore } from '@/store'
import { env } from '@/config'

defineOptions({
	name: 'Logo'
})

const appTitle = env.appTitle
const appStore = useAppStore()
const systemConfigStore = useSystemSettingStore()
const router = useRouter()
const route = useRoute()

const clickHandler = () => {
	if (route.fullPath !== systemConfigStore.menu.menuDefault) {
		router.push(systemConfigStore.menu.menuDefault)
	}
}
</script>

<style scoped>
.logo-container {
	cursor: pointer;
	margin-left: 7px;
	display: flex;
	align-items: center;
	padding: 8px;
	margin-top: 5px;
}

.logo-avatar {
	margin-right: 9px;
	color: #2c3e50;
	font-size: 18px;
	font-weight: 550;
}
</style>
