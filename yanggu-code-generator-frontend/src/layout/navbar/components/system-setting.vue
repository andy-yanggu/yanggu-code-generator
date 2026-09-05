<template>
	<icon-button :el-icon="Setting" tooltip="系统设置" size="18px" @click="visible = true"></icon-button>
	<el-drawer
		v-model="visible"
		title="系统设置"
		size="30%"
		body-class="system-setting-drawer-body"
		footer-class="system-setting-drawer-footer"
		destroy-on-close>
		<el-scrollbar>
			<div class="drawer-content">
				<div>
					<el-divider>菜单设置</el-divider>
					<setting-item v-model="systemSettingStore.menu.isOpenLogo" label="展示logo"></setting-item>
					<el-row class="setting-row">
						<el-text class="setting-label">折叠宽度</el-text>
						<div class="setting-control">
							<el-text class="setting-range">{{ MENU_FOLD_WIDTH.min }}px</el-text>
							<el-slider
								v-model="systemSettingStore.menu.menuFoldWidth"
								:min="MENU_FOLD_WIDTH.min"
								:max="MENU_FOLD_WIDTH.max"
								:format-tooltip="(val: number) => val + 'px'"
								class="setting-slider"></el-slider>
							<el-text class="setting-range">{{ MENU_FOLD_WIDTH.max }}px</el-text>
							<el-text class="setting-value">{{ systemSettingStore.menu.menuFoldWidth }}px</el-text>
						</div>
					</el-row>
					<el-row class="setting-row">
						<el-text class="setting-label">展开宽度</el-text>
						<div class="setting-control">
							<el-text class="setting-range">{{ MENU_EXPAND_WIDTH.min }}px</el-text>
							<el-slider
								v-model="systemSettingStore.menu.menuExpandWidth"
								:min="MENU_EXPAND_WIDTH.min"
								:max="MENU_EXPAND_WIDTH.max"
								:format-tooltip="(val: number) => val + 'px'"
								class="setting-slider"></el-slider>
							<el-text class="setting-range">{{ MENU_EXPAND_WIDTH.max }}px</el-text>
							<el-text class="setting-value">{{ systemSettingStore.menu.menuExpandWidth }}px</el-text>
						</div>
					</el-row>
					<setting-item
						v-model="systemSettingStore.menu.isOpenMenuCollapseAnimation"
						label="展开收起动画"
						tooltip="开启后菜单展开和收起时会有平滑过渡动画效果"></setting-item>
					<setting-item
						v-model="systemSettingStore.menu.isOpenMenuUniqueOpened"
						label="手风琴模式"
						tooltip="开启后同一时间只允许一个子菜单展开，打开新子菜单时其他子菜单自动收起"></setting-item>
					<el-row class="setting-row">
						<el-text style="margin-right: 5px">默认菜单</el-text>
						<div style="display: flex; align-items: center">
							<el-tree-select
								v-model="systemSettingStore.menu.menuDefault"
								:data="userStore.menuList"
								:props="{ label: (data: MenuInfo) => data.meta.title, value: 'path' }"
								node-key="id"
								filterable
								default-expand-all
								placeholder="请选择默认菜单"
								style="width: 180px">
								<template #default="{ data }">
									<icon-text-tooltip :icon="data.meta.icon" :title="data.meta.title"></icon-text-tooltip>
								</template>
							</el-tree-select>
						</div>
					</el-row>
				</div>
				<div>
					<el-divider>工具栏设置</el-divider>
					<setting-item v-model="systemSettingStore.toolbar.isOpenMenuCollapseButton" label="菜单展开/折叠按钮"></setting-item>
					<setting-item v-model="systemSettingStore.toolbar.isOpenBreadcrumb" label="面包屑"></setting-item>
					<setting-item v-model="systemSettingStore.toolbar.isOpenBreadcrumbIcon" label="面包屑图标"></setting-item>
					<setting-item v-model="systemSettingStore.toolbar.isOpenMenuSearch" label="菜单搜索"></setting-item>
					<setting-item v-model="systemSettingStore.toolbar.isOpenRefreshPage" label="刷新页面"></setting-item>
					<setting-item v-model="systemSettingStore.toolbar.isOpenLayoutSetting" label="布局大小"></setting-item>
					<setting-item v-model="systemSettingStore.toolbar.isOpenFullscreen" label="开启/退出全屏"></setting-item>
					<setting-item v-model="systemSettingStore.toolbar.isOpenThemeSwitch" label="日间/夜间主题切换"></setting-item>
				</div>
				<div>
					<el-divider>标签页设置</el-divider>
					<setting-item v-model="systemSettingStore.tag.isOpenTag" label="启用"></setting-item>
					<setting-item v-model="systemSettingStore.tag.isOpenTagIcon" label="图标"></setting-item>
					<setting-item
						v-model="systemSettingStore.tag.isOpenTagCache"
						label="持久化"
						tooltip="开启后即使刷新页面或重启浏览器已打开的标签页仍将保留"></setting-item>
					<setting-item
						v-model="systemSettingStore.tag.isOpenTagDragActivated"
						label="拖拽后激活标签"
						tooltip="开启后拖拽标签页位置时会自动激活被拖拽的标签页"></setting-item>
				</div>
				<div>
					<el-divider>其他设置</el-divider>
					<setting-item
						v-model="systemSettingStore.other.isOpenPageCache"
						label="页面缓存"
						tooltip="开启后切换菜单时页面数据保留，避免重复加载"></setting-item>
					<setting-item
						v-model="systemSettingStore.other.isOpenDynamicTitle"
						label="动态标题"
						tooltip="开启后页面标题会根据当前菜单动态显示"></setting-item>
					<setting-item
						v-model="systemSettingStore.other.isOpenProgress"
						label="载入进度条"
						tooltip="开启后切换菜单或者刷新页面时会看到页面顶部有进度条"></setting-item>
				</div>
				<div>
					<el-divider>菜单偏好</el-divider>
					<menu-preference-section></menu-preference-section>
				</div>
			</div>
		</el-scrollbar>
		<template #footer>
			<el-button :icon="CopyDocument" type="primary" size="small" @click="copySystemSetting()">复制设置</el-button>
			<el-button :icon="Refresh" size="small" style="margin-right: 5px" @click="handlerResetSystemSetting()">重置设置</el-button>
		</template>
	</el-drawer>
</template>

<script setup lang="ts">
import { CopyDocument, Refresh, Setting } from '@element-plus/icons-vue'
import { copyToClipboard, setDefaultTitle, setTitle } from '@/utils/tool'
import { useCacheStore, useSystemSettingStore, useTagStore, useUserStore } from '@/store'
import { MENU_EXPAND_WIDTH, MENU_FOLD_WIDTH } from '@/config'
import { MenuInfo, NavbarTag } from '@/types'
import SettingItem from '@/layout/navbar/components/system-setting-item.vue'
import MenuPreferenceSection from '@/layout/navbar/components/menu-preference-section.vue'
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'
import IconButton from '@/components/icon-button/index.vue'

defineOptions({
	name: 'SystemSetting'
})

const tagStore = useTagStore()
const cacheStore = useCacheStore()
const userStore = useUserStore()
const systemSettingStore = useSystemSettingStore()

const visible = ref(false)
const route = useRoute()

// 标签是否开启
watch(
	() => systemSettingStore.tag.isOpenTag,
	newValue => {
		// 为false的情况
		if (!newValue) {
			tagStore.removeAllTags()
		} else {
			const tag: NavbarTag = {
				fullPath: route.fullPath,
				name: route.name as string,
				title: route.meta.title as string,
				icon: route.meta.icon as string
			}
			tagStore.addTag(tag)
		}
	}
)

// 标签页缓存
watch(
	() => systemSettingStore.tag.isOpenTagCache,
	() => {
		// 开关切换时，强制触发一次 tagStore 持久化
		// serializer 会根据最新的 isOpenTagCache 决定写入内容
		tagStore.forcePersist()
	}
)

// 页面缓存
watch(
	() => systemSettingStore.other.isOpenPageCache,
	newValue => {
		// 为false的情况
		if (!newValue) {
			cacheStore.removeAllCache()
			cacheStore.removeAllIframeCache()
		}
	}
)

// 动态标题
watch(
	() => systemSettingStore.other.isOpenDynamicTitle,
	newValue => {
		// 为false的情况
		if (!newValue) {
			setDefaultTitle()
		} else {
			setTitle(route.meta.title as string)
		}
	}
)

// 复制系统设置到剪切板
const copySystemSetting = () => {
	const stateData = {
		menu: systemSettingStore.menu,
		toolbar: systemSettingStore.toolbar,
		tag: systemSettingStore.tag,
		other: systemSettingStore.other
	}
	copyToClipboard(JSON.stringify(stateData, null, 2)).then(() => {
		ElMessage.success('系统设置已复制到剪贴板')
	})
}

// 重置系统设置
const handlerResetSystemSetting = () => {
	systemSettingStore.resetSetting()
	ElMessage.success('系统设置重置成功')
}
</script>

<style lang="scss">
.system-setting-drawer-body {
	padding: 10px 5px 10px 10px;
}
.system-setting-drawer-footer {
	border-top: 1px solid var(--el-border-color);
	padding: 10px 10px 10px 15px;
}
</style>
<style scoped lang="scss">
.setting-icon {
	cursor: pointer;
}

.setting-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10px;
}

.setting-label {
	margin-right: 5px;
}

.setting-control {
	display: flex;
	align-items: center;
}

.setting-slider {
	min-width: 100px;
	flex: 1;
	margin: 0 6px;
	padding-left: 5px;
	padding-right: 5px;
	--el-slider-button-size: 15px;
}

.setting-range {
	font-size: 12px;
	color: var(--el-text-color-secondary);
}

.setting-value {
	min-width: 40px;
	text-align: right;
	margin-left: 6px;
}

.setting-tree-select {
	width: 180px;
}

.drawer-content {
	padding: 0 15px 15px 15px;
}
</style>
