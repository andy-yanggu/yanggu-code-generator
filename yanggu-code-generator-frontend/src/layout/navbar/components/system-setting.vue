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
					<el-divider>菜单设置</el-divider>
					<setting-item v-model="systemSettingStore.isOpenLogo" label="展示logo"></setting-item>
					<el-row class="setting-row">
						<el-text style="margin-right: 5px">菜单宽度</el-text>
						<div style="display: flex; align-items: center">
							<!-- 下拉推荐值 -->
							<el-select v-model="systemSettingStore.menuWidth" size="small" clearable style="width: 90px">
								<el-option
									v-for="value in menuWidthList"
									:key="value"
									:label="`${value}px`"
									:value="value"
									:disabled="systemSettingStore.menuWidth === value"
								></el-option>
							</el-select>
						</div>
					</el-row>
					<setting-item
						v-model="systemSettingStore.isOpenMenuCollapseAnimation"
						label="展开收起动画"
						tooltip="开启后菜单展开和收起时会有平滑过渡动画效果"
					></setting-item>
					<setting-item
						v-model="systemSettingStore.isOpenMenuUniqueOpened"
						label="手风琴模式"
						tooltip="开启后同一时间只允许一个子菜单展开，打开新子菜单时其他子菜单自动收起"
					></setting-item>
				</div>
				<div>
					<el-divider>工具栏设置</el-divider>
					<setting-item v-model="systemSettingStore.isOpenMenuCollapseButton" label="菜单展开/折叠按钮"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenBreadcrumb" label="面包屑"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenBreadcrumbIcon" label="面包屑图标"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenMenuSearch" label="菜单搜索"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenRefreshPage" label="刷新页面"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenLayoutSetting" label="布局大小"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenFullscreen" label="开启/退出全屏"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenThemeSwitch" label="日间/夜间主题切换"></setting-item>
				</div>
				<div>
					<el-divider>标签设置</el-divider>
					<setting-item v-model="systemSettingStore.isOpenTag" label="启用"></setting-item>
					<setting-item v-model="systemSettingStore.isOpenTagIcon" label="图标"></setting-item>
					<setting-item
						v-model="systemSettingStore.isOpenTagCache"
						label="持久化"
						tooltip="开启后即使刷新页面或重启浏览器已打开的标签页仍将保留"
					></setting-item>
				</div>
				<div>
					<el-divider>其他设置</el-divider>
					<setting-item
						v-model="systemSettingStore.isOpenPageCache"
						label="页面缓存"
						tooltip="开启后切换菜单时页面数据保留，避免重复加载"
					></setting-item>
					<setting-item
						v-model="systemSettingStore.isOpenDynamicTitle"
						label="动态标题"
						tooltip="开启后页面标题会根据当前菜单动态显示"
					></setting-item>
					<setting-item
						v-model="systemSettingStore.isOpenProgress"
						label="载入进度条"
						tooltip="开启后切换菜单或者刷新页面时会看到页面顶部有进度条"
					></setting-item>
				</div>
			</div>
		</el-scrollbar>
		<template #footer>
			<el-button icon="CopyDocument" type="primary" size="small" @click="copySystemSetting()">复制设置</el-button>
			<el-button icon="Refresh" size="small" @click="handlerResetSystemSetting()">重置设置</el-button>
		</template>
	</el-drawer>
</template>

<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import { copyToClipboard, setDefaultTitle, setTitle } from '@/utils/tool'
import { useRoute } from 'vue-router'
import { useSystemSettingStore } from '@/store/system-setting-store'
import { NavbarTag, useAppStore } from '@/store/app-store'
import { ElMessage } from 'element-plus'
import SettingItem from '@/components/setting-item/index.vue'

defineOptions({
	name: 'SystemSetting'
})

const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()

const visible = ref(false)
const route = useRoute()

const menuWidthList = ref([180, 210, 240])

// 标签是否开启
watch(
	() => systemSettingStore.isOpenTag,
	newValue => {
		// 为false的情况
		if (!newValue) {
			appStore.removeAllTags()
		} else {
			const tag = {
				fullPath: route.fullPath,
				name: route.name as string,
				title: route.meta.title as string,
				icon: route.meta.icon as string
			} as NavbarTag
			appStore.addTag(tag)
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

// 复制系统设置到剪切板
const copySystemSetting = () => {
	const stateData = { ...systemSettingStore.$state }
	copyToClipboard(JSON.stringify(stateData, null, 2)).then(() => {
		ElMessage.success('系统设置已复制到剪贴板')
	})
}

// 重置系统设置
const handlerResetSystemSetting = () => {
	systemSettingStore.resetSettings()
	ElMessage.success('系统设置重置成功')
}
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
