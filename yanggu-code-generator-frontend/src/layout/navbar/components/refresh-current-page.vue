<template>
	<div class="refresh-container">
		<el-tooltip :content="'刷新页面'" effect="dark" placement="bottom">
			<el-icon :size="18" class="refresh-icon" @click="refreshPage()">
				<Refresh></Refresh>
			</el-icon>
		</el-tooltip>
	</div>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
import { usePageRefresher } from '@/hooks/use-refresh-current-page'

defineOptions({
	name: 'RefreshCurrentPage'
})
const { refreshPage } = usePageRefresher()
</script>
<style scoped>
.refresh-icon {
	cursor: pointer;
}
.refresh-container {
	display: flex;
	align-items: center;
}
</style>
