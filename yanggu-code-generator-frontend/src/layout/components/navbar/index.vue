:index.vue
<template>
	<div>
		<div class="navbar-container">
			<!-- 左侧区域：折叠按钮 + 面包屑 -->
			<div class="navbar-left">
				<el-icon :size="22" @click="store.toggleCollapse()">
					<Expand v-if="store.isCollapseRef"></Expand>
					<Fold v-else></Fold>
				</el-icon>
				<breadcrumb></breadcrumb>
			</div>

			<!-- 右侧区域：链接 + 全屏按钮 -->
			<div class="navbar-right">
				<el-tooltip :content="'gitee地址'" effect="dark" placement="bottom">
					<el-link href="https://gitee.com/andy_yanggu/yanggu-code-generator" target="_blank">
						<svg-icon icon="icon-gitee-fill-round"></svg-icon>
					</el-link>
				</el-tooltip>
				<el-tooltip :content="'github地址'" effect="dark" placement="bottom">
					<el-link href="https://github.com/andy-yanggu/yanggu-code-generator" target="_blank">
						<svg-icon icon="icon-github-fill"></svg-icon>
					</el-link>
				</el-tooltip>
				<el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" effect="dark" placement="bottom">
					<el-icon :size="20" @click="toggleFullscreen">
						<FullScreen v-if="!isFullscreen" />
						<Aim v-else />
					</el-icon>
				</el-tooltip>
			</div>
		</div>
		<!-- 标签栏 -->
		<tag></tag>
	</div>
</template>

<script setup lang="ts">
import { Aim, Expand, Fold, FullScreen } from '@element-plus/icons-vue'
import Tag from '@/layout/components/navbar/components/tag.vue'
import { useAppStore } from '@/store/app-store'
import { onMounted, onUnmounted, ref } from 'vue'
import SvgIcon from '@/components/svg-icon/src/svg-icon.vue'
import Breadcrumb from '@/layout/components/navbar/components/breadcrumb.vue'
const isFullscreen = ref(false)
const store = useAppStore()
// 全屏切换函数
const toggleFullscreen = () => {
	if (!document.fullscreenElement) {
		document.documentElement
			.requestFullscreen()
			.then(() => (isFullscreen.value = true))
			.catch(err => console.error('全屏失败:', err))
	} else {
		document
			.exitFullscreen()
			.then(() => (isFullscreen.value = false))
			.catch(err => console.error('退出全屏失败:', err))
	}
}

// 监听全屏状态变化
const handleFullscreenChange = () => {
	isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
	document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
	document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style scoped>
.navbar-container {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 10px;
}

.navbar-left {
	display: flex;
	align-items: center;
	gap: 16px;
}
.navbar-right {
	display: flex;
	align-items: center;
	gap: 16px;
	margin-left: auto;
	margin-right: 40px;
}
</style>
