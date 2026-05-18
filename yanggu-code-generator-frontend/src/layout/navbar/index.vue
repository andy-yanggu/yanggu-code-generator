<template>
	<div>
		<!-- 工具栏 -->
		<div class="navbar-toolbar">
			<!-- 左侧区域：折叠按钮 + 面包屑 -->
			<el-space :size="15">
				<!-- 折叠按钮 -->
				<toggle-collapse v-if="systemSettingStore.toolbar.isOpenMenuCollapseButton"></toggle-collapse>
				<!-- 面包屑 -->
				<breadcrumb v-if="systemSettingStore.toolbar.isOpenBreadcrumb"></breadcrumb>
			</el-space>

			<!-- 右侧区域 -->
			<el-space :size="15">
				<!-- 菜单搜索 -->
				<menu-search v-if="systemSettingStore.toolbar.isOpenMenuSearch"></menu-search>
				<!-- 刷新当前页 -->
				<refresh-current-page v-if="systemSettingStore.toolbar.isOpenRefreshPage"></refresh-current-page>
				<!-- 切换布局大小	-->
				<layout-size v-if="systemSettingStore.toolbar.isOpenLayoutSetting"></layout-size>
				<!-- 全屏按钮 -->
				<full-screen v-if="systemSettingStore.toolbar.isOpenFullscreen"></full-screen>
				<!-- 日间/夜间模式切换  -->
				<theme-switch v-if="systemSettingStore.toolbar.isOpenThemeSwitch"></theme-switch>
				<!-- 系统设置 -->
				<system-setting></system-setting>
				<!-- 用户下拉菜单 -->
				<user-dropdown></user-dropdown>
			</el-space>
		</div>
		<!-- 标签栏 -->
		<tag-bar v-if="systemSettingStore.tag.isOpenTag"></tag-bar>
	</div>
</template>

<script setup lang="ts">
import TagBar from '@/layout/navbar/components/tag-bar.vue'
import Breadcrumb from '@/layout/navbar/components/breadcrumb.vue'
import MenuSearch from '@/layout/navbar/components/menu-search.vue'
import SystemSetting from '@/layout/navbar/components/system-setting.vue'
import RefreshCurrentPage from '@/layout/navbar/components/refresh-current-page.vue'
import LayoutSize from '@/layout/navbar/components/layout-size.vue'
import ThemeSwitch from '@/layout/navbar/components/theme-switch.vue'
import UserDropdown from '@/layout/navbar/components/user-dropdown.vue'
import { useSystemSettingStore } from '@/store'
import FullScreen from '@/layout/navbar/components/full-screen.vue'
import ToggleCollapse from '@/layout/navbar/components/toggle-collapse.vue'

defineOptions({
	name: 'LayoutNavbar'
})

const systemSettingStore = useSystemSettingStore()
</script>
