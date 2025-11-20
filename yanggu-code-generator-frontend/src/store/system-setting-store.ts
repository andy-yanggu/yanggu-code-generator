import { defineStore } from 'pinia'
import { reactive, toRefs } from 'vue'
import { cloneObject, resetReactiveObject } from '@/utils/tool'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { SystemSetting } from '@/types'

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
