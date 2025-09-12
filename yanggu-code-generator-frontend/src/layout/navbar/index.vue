<template>
	<div>
		<!-- 工具栏 -->
		<div class="navbar-toolbar">
			<!-- 左侧区域：折叠按钮 + 面包屑 -->
			<div class="navbar-toolbar-left">
				<el-icon v-if="systemSettingStore.isOpenMenuCollapseButton" :size="18" class="collapse-icon" @click="appStore.toggleCollapse()">
					<Expand v-if="appStore.isCollapse"></Expand>
					<Fold v-else></Fold>
				</el-icon>
				<breadcrumb v-if="systemSettingStore.isOpenBreadcrumb"></breadcrumb>
			</div>

			<!-- 右侧区域：菜单搜索 + 刷新 + 切换布局大小 + 链接 + 全屏按钮 -->
			<div class="navbar-toolbar-right">
				<!-- 菜单搜索 -->
				<menu-search v-if="systemSettingStore.isOpenMenuSearch"></menu-search>
				<!-- 刷新当前页 -->
				<refresh-current-page v-if="systemSettingStore.isOpenRefreshPage"></refresh-current-page>
				<!-- 切换布局大小	-->
				<layout-size v-if="systemSettingStore.isOpenLayoutSetting"></layout-size>
				<!-- 全屏按钮 -->
				<el-tooltip v-if="systemSettingStore.isOpenFullscreen" :content="isFullscreen ? '退出全屏' : '开启全屏'" effect="dark" placement="bottom">
					<svg-icon :icon="isFullscreen ? 'icon-fullscreen-exit' : 'icon-fullscreen'" size="18px" is-pointer @click="toggle()"></svg-icon>
				</el-tooltip>
				<!-- 日间/夜间模式切换  -->
				<theme-switch v-if="systemSettingStore.isOpenThemeSwitch"></theme-switch>
				<!-- 系统设置 -->
				<system-setting></system-setting>
			</div>
		</div>
		<!-- 标签栏 -->
		<tag v-if="systemSettingStore.isOpenTag"></tag>
	</div>
</template>

<script setup lang="ts">
import { Expand, Fold } from '@element-plus/icons-vue'
import { useAppStore } from '@/store/app-store'
import Tag from '@/layout/navbar/components/tag.vue'
import Breadcrumb from '@/layout/navbar/components/breadcrumb.vue'
import MenuSearch from '@/layout/navbar/components/menu-search.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import SystemSetting from '@/layout/navbar/components/system-setting.vue'
import RefreshCurrentPage from '@/layout/navbar/components/refresh-current-page.vue'
import LayoutSize from '@/layout/navbar/components/layout-size.vue'
import { useFullscreen } from '@vueuse/core'
import { useSystemSettingStore } from '@/store/system-setting-store'
import ThemeSwitch from '@/layout/navbar/components/theme-switch.vue'

defineOptions({
	name: 'LayoutNavbar'
})

const { isFullscreen, toggle } = useFullscreen()
const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()
</script>

<style scoped>
.navbar-toolbar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 15px;
	margin-bottom: 15px;
}

.navbar-toolbar-left {
	display: flex;
	align-items: center;
	gap: 15px;
}

.navbar-toolbar-right {
	display: flex;
	align-items: center;
	gap: 15px;
	margin-left: auto;
}

.collapse-icon {
	cursor: pointer;
}
</style>
