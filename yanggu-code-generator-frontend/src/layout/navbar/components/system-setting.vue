<template>
	<el-tooltip content="系统设置" effect="dark" placement="bottom">
		<el-icon size="18px" class="setting-icon" @click="visible = true">
			<Setting></Setting>
		</el-icon>
	</el-tooltip>
	<el-drawer v-model="visible" title="系统设置" size="20%" destroy-on-close>
		<el-scrollbar>
			<div class="drawer-content">
				<div>
					<el-divider>菜单</el-divider>
					<el-row class="setting-row">
						<el-text>展示logo</el-text>
						<el-switch v-model="systemSettingStore.isOpenLogo" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>折叠动画</el-text>
						<el-switch v-model="systemSettingStore.isOpenMenuCollapseAnimation" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>只保持一个子菜单的展开</el-text>
						<el-switch v-model="systemSettingStore.isOpenMenuUniqueOpened" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
				</div>
				<div>
					<el-divider>工具栏</el-divider>
					<el-row class="setting-row">
						<el-text>菜单展开/折叠按钮</el-text>
						<el-switch v-model="systemSettingStore.isOpenMenuCollapseButton" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>面包屑</el-text>
						<el-switch v-model="systemSettingStore.isOpenBreadcrumb" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>面包屑图标</el-text>
						<el-switch v-model="systemSettingStore.isOpenBreadcrumbIcon" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>菜单搜索</el-text>
						<el-switch v-model="systemSettingStore.isOpenMenuSearch" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>刷新页面</el-text>
						<el-switch v-model="systemSettingStore.isOpenRefreshPage" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>布局大小</el-text>
						<el-switch v-model="systemSettingStore.isOpenLayoutSetting" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>开启/退出全屏</el-text>
						<el-switch v-model="systemSettingStore.isOpenFullscreen" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
				</div>
				<div>
					<el-divider>标签栏</el-divider>
					<el-row class="setting-row">
						<el-text>启用</el-text>
						<el-switch v-model="systemSettingStore.isOpenTag" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>图标</el-text>
						<el-switch v-model="systemSettingStore.isOpenTagIcon" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<el-text>持久化</el-text>
						<el-switch v-model="systemSettingStore.isOpenTagCache" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
				</div>
				<div>
					<el-divider>其他</el-divider>
					<el-row class="setting-row">
						<div>
							<el-text style="margin-right: 5px">页面缓存</el-text>
							<el-tooltip content="该功能开启时，切换菜单时页面数据保留" effect="dark" placement="top">
								<el-icon class="setting-icon"><InfoFilled></InfoFilled></el-icon>
							</el-tooltip>
						</div>
						<el-switch v-model="systemSettingStore.isOpenPageCache" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<div>
							<el-text style="margin-right: 5px">动态标题</el-text>
							<el-tooltip content="该功能开启时，页面标题会根据当前菜单动态显示" effect="dark" placement="top">
								<el-icon class="setting-icon"><InfoFilled></InfoFilled></el-icon>
							</el-tooltip>
						</div>
						<el-switch v-model="systemSettingStore.isOpenDynamicTitle" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
					<el-row class="setting-row">
						<div>
							<el-text style="margin-right: 5px">载入进度条</el-text>
							<el-tooltip content="该功能开启时，菜单切换会看到页面顶部有进度条" effect="dark" placement="top">
								<el-icon class="setting-icon"><InfoFilled></InfoFilled></el-icon>
							</el-tooltip>
						</div>
						<el-switch v-model="systemSettingStore.isOpenProgress" inline-prompt active-text="开" inactive-text="关"></el-switch>
					</el-row>
				</div>
			</div>
		</el-scrollbar>
	</el-drawer>
</template>

<script setup lang="ts">
import { InfoFilled, Setting } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import { setDefaultTitle, setTitle } from '@/utils/tool'
import { useRoute } from 'vue-router'
import { useSystemSettingStore } from '@/store/system-setting-store'
import { useAppStore } from '@/store/app-store'

const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()

const visible = ref(false)
const route = useRoute()

// 标签是否开启
watch(
	() => systemSettingStore.isOpenTag,
	newValue => {
		// 为false的情况
		if (!newValue) {
			appStore.removeAllTags()
		}
	}
)

// 页面缓存
watch(
	() => systemSettingStore.isOpenPageCache,
	newValue => {
		// 为false的情况
		if (!newValue) {
			appStore.removeAllCache()
			appStore.removeAllIframeCache()
		}
	}
)

// 动态标题
watch(
	() => systemSettingStore.isOpenDynamicTitle,
	newValue => {
		// 为false的情况
		if (!newValue) {
			setDefaultTitle()
		} else {
			setTitle(route.meta.title as string)
		}
	}
)
</script>

<style scoped>
.setting-icon {
	cursor: pointer;
}

.setting-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10px;
}

.drawer-content {
	padding: 0 15px 15px 15px;
}
</style>
