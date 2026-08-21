<template>
	<icon-button
		:el-icon="Refresh"
		:loading-icon="Refresh"
		:loading="appStore.globalLoading"
		tooltip="刷新页面"
		size="18px"
		@click="refreshPage()"></icon-button>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
import { usePageRefresher } from '@/hooks'
import { useAppStore } from '@/store'
import IconButton from '@/components/icon-button/index.vue'

defineOptions({
	name: 'RefreshCurrentPage'
})
const appStore = useAppStore()
const { refreshPage } = usePageRefresher()
</script>
