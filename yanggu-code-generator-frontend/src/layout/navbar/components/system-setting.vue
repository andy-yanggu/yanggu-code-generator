<template>
	<el-tooltip content="系统设置" effect="dark" placement="bottom">
		<el-icon :size="18" class="setting-icon" @click="clickHandler()"><Setting></Setting></el-icon>
	</el-tooltip>
	<el-drawer v-model="state.visible" title="系统设置" destroy-on-close>
		<div>
			<el-divider>菜单</el-divider>
			<el-row>
				<el-text>是否展示logo</el-text>
				<el-switch v-model="systemSettingStore.isOpenLogo"></el-switch>
			</el-row>
			<el-row>
				<el-text>是否开启折叠动画</el-text>
				<el-switch v-model="systemSettingStore.isOpenMenuCollapseAnimation"></el-switch>
			</el-row>
			<el-row>
				<el-text>是否只保持一个子菜单的展开</el-text>
				<el-switch v-model="systemSettingStore.isOpenMenuUniqueOpened"></el-switch>
			</el-row>
		</div>
		<div>
			<el-divider>面包屑</el-divider>
			<el-row>
				<el-text>是否启用</el-text>
				<el-switch v-model="systemSettingStore.isOpenBreadcrumb"></el-switch>
			</el-row>
			<el-row>
				<el-text>是否启用图标</el-text>
				<el-switch v-model="systemSettingStore.isOpenBreadcrumbIcon"></el-switch>
			</el-row>
		</div>
		<div>
			<el-divider>工具栏</el-divider>
			<el-row>
				<el-text>菜单搜索</el-text>
				<el-switch v-model="systemSettingStore.isOpenMenuSearch"></el-switch>
			</el-row>
			<el-row>
				<el-text>刷新页面</el-text>
				<el-switch v-model="systemSettingStore.isOpenRefreshPage"></el-switch>
			</el-row>
			<el-row>
				<el-text>布局大小</el-text>
				<el-switch v-model="systemSettingStore.isOpenLayoutSetting"></el-switch>
			</el-row>
			<el-row>
				<el-text>切换全屏</el-text>
				<el-switch v-model="systemSettingStore.isOpenFullscreen"></el-switch>
			</el-row>
		</div>
		<div>
			<el-divider>标签栏</el-divider>
			<el-row>
				<el-text>是否启用</el-text>
				<el-switch v-model="systemSettingStore.isOpenTag"></el-switch>
			</el-row>
			<el-row>
				<el-text>是否启用图标</el-text>
				<el-switch v-model="systemSettingStore.isOpenTagIcon"></el-switch>
			</el-row>
			<el-row>
				<el-text>是否持久化</el-text>
				<el-switch v-model="systemSettingStore.isOpenTagCache"></el-switch>
			</el-row>
		</div>
		<div>
			<el-divider>其他</el-divider>
			<el-row>
				<el-text>开启动态标题</el-text>
				<el-switch v-model="systemSettingStore.isOpenDynamicTitle"></el-switch>
			</el-row>
			<el-row>
				<el-text>载入进度条</el-text>
				<el-switch v-model="systemSettingStore.isOpenProgress"></el-switch>
			</el-row>
		</div>
	</el-drawer>
</template>

<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'
import { reactive, watch } from 'vue'
import { useSystemSettingStore } from '@/store/system-setting-store'
import { setDefaultTitle, setTitle } from '@/utils/tool'
import { useRoute } from 'vue-router'

const systemSettingStore = useSystemSettingStore()

const state = reactive({
	visible: false
})

const clickHandler = () => {
	state.visible = true
}

const route = useRoute()

watch(
	() => systemSettingStore.isOpenDynamicTitle,
	newValue => {
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
</style>
