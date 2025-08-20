<template>
	<div>
		<div class="navbar-container">
			<!-- 左侧区域：折叠按钮 + 面包屑 -->
			<div class="navbar-left">
				<el-icon :size="18" class="collapse-icon" @click="appStore.toggleCollapse()">
					<Expand v-if="appStore.isCollapseRef"></Expand>
					<Fold v-else></Fold>
				</el-icon>
				<breadcrumb></breadcrumb>
			</div>

			<!-- 右侧区域：搜索 + 刷新 + 链接 + 全屏按钮 -->
			<div class="navbar-right">
				<menu-search></menu-search>
				<refresh-current-page></refresh-current-page>
				<el-tooltip :content="'gitee地址'" effect="dark" placement="bottom">
					<el-link href="https://gitee.com/andy_yanggu/yanggu-code-generator" target="_blank">
						<svg-icon icon="icon-gitee-fill-round" size="18px"></svg-icon>
					</el-link>
				</el-tooltip>
				<el-tooltip :content="'github地址'" effect="dark" placement="bottom">
					<el-link href="https://github.com/andy-yanggu/yanggu-code-generator" target="_blank">
						<svg-icon icon="icon-github-fill" size="18px"></svg-icon>
					</el-link>
				</el-tooltip>
				<el-tooltip :content="isFullscreen ? '还原' : '全屏'" effect="dark" placement="bottom">
					<svg-icon :icon="isFullscreen ? 'icon-fullscreen' : 'icon-fullscreen-exit'" size="18px" class="collapse-icon" @click="toggle()"></svg-icon>
				</el-tooltip>
			</div>
		</div>
		<!-- 标签栏 -->
		<tag></tag>
	</div>
</template>

<script setup lang="ts">
import { Aim, Expand, Fold, FullScreen } from '@element-plus/icons-vue'
import { useAppStore } from '@/store/app-store'
import Tag from '@/layout/navbar/components/tag.vue'
import Breadcrumb from '@/layout/navbar/components/breadcrumb.vue'
import MenuSearch from '@/layout/navbar/components/menu-search.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import RefreshCurrentPage from '@/layout/navbar/components/refresh-current-page.vue'
import { useFullscreen } from '@vueuse/core'

const { isFullscreen, toggle } = useFullscreen()
const appStore = useAppStore()
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
	gap: 15px;
}

.navbar-right {
	display: flex;
	align-items: center;
	gap: 16px;
	margin-left: auto;
	margin-right: 40px;
}

.collapse-icon {
	cursor: pointer;
}
</style>
