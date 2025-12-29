<template>
	<el-tooltip :content="'刷新页面'" effect="dark" placement="bottom">
		<el-button
			link
			:icon="Refresh"
			:loading-icon="Refresh"
			:loading="appStore.globalLoading"
			style="font-size: 18px; color: inherit"
			@click="refreshPage()"
		></el-button>
	</el-tooltip>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
import { usePageRefresher } from '@/hooks'
import { useAppStore } from '@/store'

defineOptions({
	name: 'RefreshCurrentPage'
})
const appStore = useAppStore()
const { refreshPage } = usePageRefresher()
</script>
