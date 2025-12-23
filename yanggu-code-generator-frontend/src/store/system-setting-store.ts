import { defineStore } from 'pinia'
import { reactive, toRefs } from 'vue'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { MenuSetting, OtherSetting, TagBarSetting, ToolbarSetting } from '@/types'

// 菜单展开宽度
export const menuExpandWidthList = [180, 210, 240]

// 菜单折叠宽度
export const menuFoldWidthList = [50, 60, 70]

// 默认配置
// 菜单相关默认设置
const defaultMenuSetting = (): MenuSetting => ({
	isOpenLogo: true,
	isOpenMenuCollapseAnimation: true,
	isOpenMenuUniqueOpened: false,
	isOpenMenuCollapseButton: true,
	menuExpandWidth: menuExpandWidthList.length >= 3 ? menuExpandWidthList[1] : menuExpandWidthList[0],
	menuFoldWidth: menuFoldWidthList.length >= 3 ? menuFoldWidthList[1] : menuFoldWidthList[0],
	menuDefault: '/index'
})

// 工具栏相关默认设置
const defaultToolbarSetting = (): ToolbarSetting => ({
	isOpenMenuCollapseButton: true,
	isOpenMenuSearch: true,
	isOpenRefreshPage: true,
	isOpenLayoutSetting: true,
	isOpenFullscreen: true,
	isOpenThemeSwitch: true,
	isOpenBreadcrumb: true,
	isOpenBreadcrumbIcon: true
})

// 标签页相关默认设置
const defaultTagSetting = (): TagBarSetting => ({
	isOpenTag: true,
	isOpenTagIcon: true,
	isOpenTagCache: true,
	isOpenTagDragActivated: false
})

// 其他设置默认值
const defaultOtherSetting = (): OtherSetting => ({
	isOpenDynamicTitle: true,
	isOpenProgress: true,
	isOpenPageCache: true
})

// 持久化配置
const getPersistConfig = () => {
	const key = 'systemSettingStore'
	// 根据环境设置
	// if (import.meta.env.PROD) {
	// }
	return {
		key,
		storage: localStorage
	} as PersistenceOptions
}

export const useSystemSettingStore = defineStore(
	'system-setting',
	() => {
		const state = reactive({
			menu: defaultMenuSetting(),
			toolbar: defaultToolbarSetting(),
			tag: defaultTagSetting(),
			other: defaultOtherSetting()
		})

		// 恢复默认配置
		const resetSetting = () => {
			Object.assign(state.menu, defaultMenuSetting())
			Object.assign(state.toolbar, defaultToolbarSetting())
			Object.assign(state.tag, defaultTagSetting())
			Object.assign(state.other, defaultOtherSetting())
		}

		return {
			// 注意这里要用 toRefs 保持响应式
			...toRefs(state),
			resetSetting
		}
	},
	{
		persist: getPersistConfig()
	}
)
