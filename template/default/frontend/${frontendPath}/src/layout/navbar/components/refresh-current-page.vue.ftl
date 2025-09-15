<template>
	<div class="refresh-container">
		<el-tooltip :content="'刷新页面'" effect="dark" placement="bottom">
			<el-icon :size="18" class="refresh-icon" @click="refreshHandler()">
				<Refresh></Refresh>
			</el-icon>
		</el-tooltip>
	</div>
</template>
<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app-store'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

defineOptions({
	name: 'RefreshCurrentPage'
})

const refreshHandler = () => {
	const resolve = router.resolve(route.path)
	if (resolve && resolve.name) {
		// 删除缓存组件
		appStore.removeCacheComponent(resolve.name as string)
	}
	// 使用路由跳转实现刷新
	router.push({
		path: '/redirect' + route.fullPath,
		query: route.query
	})
}
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
