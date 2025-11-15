import { defineStore } from 'pinia'
import { reactive, toRefs } from 'vue'
import { cloneObject, resetReactiveObject } from '@/utils/tool'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'

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
	// 是否开启被激活拖拽标签
	isOpenTagDragActivated: boolean
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
	// 主题切换
	isOpenThemeSwitch: boolean
	// 是否开启菜单折叠动画
	isOpenMenuCollapseAnimation: boolean
	// 是否开启菜单只打开一个菜单
	isOpenMenuUniqueOpened: boolean
	// 菜单展开/折叠按钮
	isOpenMenuCollapseButton: boolean
	// 菜单展开宽度
	menuExpandWidth: number
	// 菜单折叠宽度
	menuFoldWidth: number
	// 默认菜单
	menuDefault: string
}

// 菜单展开宽度
export const menuExpandWidthList = [180, 210, 240]

// 菜单折叠宽度
export const menuFoldWidthList = [50, 60, 70]

// 默认配置
const defaultSystemSetting: SystemSetting = {
	isOpenBreadcrumb: true,
	isOpenBreadcrumbIcon: true,
	isOpenTag: true,
	isOpenTagIcon: true,
	isOpenTagCache: true,
	isOpenTagDragActivated: false,
	isOpenDynamicTitle: true,
	isOpenLogo: true,
	isOpenProgress: true,
	isOpenPageCache: true,
	isOpenMenuSearch: true,
	isOpenRefreshPage: true,
	isOpenLayoutSetting: true,
	isOpenFullscreen: true,
	isOpenThemeSwitch: true,
	isOpenMenuCollapseAnimation: true,
	isOpenMenuUniqueOpened: false,
	isOpenMenuCollapseButton: true,
	menuExpandWidth: menuExpandWidthList.length >= 3 ? menuExpandWidthList[1] : menuExpandWidthList[0],
	menuFoldWidth: menuFoldWidthList.length >= 3 ? menuFoldWidthList[1] : menuFoldWidthList[0],
	menuDefault: '/index'
}

// 持久化配置
const getPersistConfig = () => {
	const key = 'systemSettingStore'
	if (import.meta.env.PROD) {
		return {
			key,
			storage: localStorage
		} as PersistenceOptions
	} else {
		return false
	}
}

export const useSystemSettingStore = defineStore(
	'system-setting',
	() => {
		const state = reactive<SystemSetting>(cloneObject(defaultSystemSetting))

		// 恢复默认配置
		const resetSettings = () => {
			resetReactiveObject(state, defaultSystemSetting)
		}

		return {
			// 注意这里要用 toRefs 保持响应式
			...toRefs(state),
			resetSettings
		}
	},
	{
		persist: getPersistConfig()
	}
)
