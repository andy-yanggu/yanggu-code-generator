<template>
	<div>
		<!-- 工具栏 -->
		<div class="navbar-toolbar">
			<!-- 左侧区域：折叠按钮 + 面包屑 -->
			<div class="navbar-toolbar-left">
				<!-- 折叠按钮 -->
				<el-icon v-if="systemSettingStore.isOpenMenuCollapseButton" :size="18" class="collapse-icon" @click="appStore.toggleCollapse()">
					<Expand v-if="appStore.isCollapse"></Expand>
					<Fold v-else></Fold>
				</el-icon>
				<!-- 面包屑 -->
				<breadcrumb v-if="systemSettingStore.isOpenBreadcrumb"></breadcrumb>
			</div>

			<!-- 右侧区域 -->
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
				<!-- 用户下拉菜单 -->
				<user-dropdown></user-dropdown>
				<!-- 系统设置 -->
				<system-setting></system-setting>
			</div>
		</div>
		<!-- 标签栏 -->
		<tag-bar v-if="systemSettingStore.isOpenTag"></tag-bar>
	</div>
</template>

<script setup lang="ts">
import { Expand, Fold } from '@element-plus/icons-vue'
import TagBar from '@/layout/navbar/components/tag-bar.vue'
import Breadcrumb from '@/layout/navbar/components/breadcrumb.vue'
import MenuSearch from '@/layout/navbar/components/menu-search.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import SystemSetting from '@/layout/navbar/components/system-setting.vue'
import RefreshCurrentPage from '@/layout/navbar/components/refresh-current-page.vue'
import LayoutSize from '@/layout/navbar/components/layout-size.vue'
import ThemeSwitch from '@/layout/navbar/components/theme-switch.vue'
import UserDropdown from '@/layout/navbar/components/user-dropdown.vue'
import { useFullscreen } from '@vueuse/core'
import { useAppStore, useSystemSettingStore } from '@/store'

defineOptions({
	name: 'LayoutNavbar'
})

const { isFullscreen, toggle } = useFullscreen()
const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()
</script>

<style scoped>
.navbar-toolbar {
	height: 35px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 10px;
	margin-top: 6px;
	border-bottom: 1px solid var(--el-border-color);
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
}

.collapse-icon {
	cursor: pointer;
}
</style>
