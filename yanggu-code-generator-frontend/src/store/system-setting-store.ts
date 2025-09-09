import { defineStore } from 'pinia'
import { reactive, toRefs } from 'vue'

interface SystemSetting {
	// 是否开启面包屑
	isOpenBreadcrumb: boolean
	// 面包屑是否设置图标
	isOpenBreadcrumbIcon: boolean
	// 是否开启标签页
	isOpenTag: boolean
	// 是否开启标签页图标
	isOpenTagIcon: boolean
	// 是否开启tag持久化
	isOpenTagCache: boolean
	// 是否设置动态标题
	isOpenDynamicTitle: boolean
	// 是否开启logo
	isOpenLogo: boolean
	// 是否开启载入进度条
	isOpenProgress: boolean
	// 是否开启页面缓存
	isOpenPageCache: boolean
	// 是否开启菜单搜索
	isOpenMenuSearch: boolean
	// 是否开启刷新页面
	isOpenRefreshPage: boolean
	// 布局大小
	isOpenLayoutSetting: boolean
	// 切换全屏
	isOpenFullscreen: boolean
	// 是否开启菜单折叠动画
	isOpenMenuCollapseAnimation: boolean
	// 是否开启菜单只打开一个菜单
	isOpenMenuUniqueOpened: boolean
	// 菜单展开/折叠按钮
	isOpenMenuCollapseButton: boolean
	// 菜单宽度
	menuWidth: number
}

// 默认配置
const defaultSystemSetting: SystemSetting = {
	isOpenBreadcrumb: true,
	isOpenBreadcrumbIcon: true,
	isOpenTag: true,
	isOpenTagIcon: true,
	isOpenTagCache: true,
	isOpenDynamicTitle: true,
	isOpenLogo: true,
	isOpenProgress: true,
	isOpenPageCache: true,
	isOpenMenuSearch: true,
	isOpenRefreshPage: true,
	isOpenLayoutSetting: true,
	isOpenFullscreen: true,
	isOpenMenuCollapseAnimation: true,
	isOpenMenuUniqueOpened: true,
	isOpenMenuCollapseButton: true,
	menuWidth: 210
}

export const useSystemSettingStore = defineStore(
	'system-setting',
	() => {
		const state = reactive<SystemSetting>({ ...defaultSystemSetting })

		// 恢复默认配置
		const resetSettings = () => {
			Object.assign(state, defaultSystemSetting)
		}

		// 注意这里要用 toRefs 保持响应式
		return {
			...toRefs(state),
			resetSettings
		}
	},
	{
		persist: {
			key: 'systemSettingStore',
			storage: localStorage
		}
	}
)
