import { defineStore } from 'pinia'
import { ref } from 'vue'

const defaultSystemSetting: SystemSetting = {
	isOpenBreadcrumb: true,
	isOpenBreadcrumbIcon: true,
	isOpenTag: true,
	isOpenTagIcon: true,
	isOpenTagCache: true,
	isOpenDynamicTitle: true,
	isOpenLogo: true,
	isOpenProgress: true,
	isOpenCache: true,
	isOpenMenuSearch: true,
	isOpenRefreshPage: true,
	isOpenLayoutSetting: true,
	isOpenFullscreen: true,
	isOpenMenuCollapseAnimation: true,
	isOpenMenuUniqueOpened: true
} as SystemSetting

export interface SystemSetting {
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
	// 是否开启缓存
	isOpenCache: boolean
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
}

export const useSystemSettingStore = defineStore('system-setting', () => {
	// 状态
	const isOpenBreadcrumb = ref(defaultSystemSetting.isOpenBreadcrumb)
	const isOpenBreadcrumbIcon = ref(defaultSystemSetting.isOpenBreadcrumbIcon)
	const isOpenTag = ref(defaultSystemSetting.isOpenTag)
	const isOpenTagIcon = ref(defaultSystemSetting.isOpenTagIcon)
	const isOpenTagCache = ref(defaultSystemSetting.isOpenTagCache)
	const isOpenDynamicTitle = ref(defaultSystemSetting.isOpenDynamicTitle)
	const isOpenLogo = ref(defaultSystemSetting.isOpenLogo)
	const isOpenProgress = ref(defaultSystemSetting.isOpenProgress)
	const isOpenCache = ref(defaultSystemSetting.isOpenCache)
	const isOpenMenuSearch = ref(defaultSystemSetting.isOpenMenuSearch)
	const isOpenRefreshPage = ref(defaultSystemSetting.isOpenRefreshPage)
	const isOpenLayoutSetting = ref(defaultSystemSetting.isOpenLayoutSetting)
	const isOpenFullscreen = ref(defaultSystemSetting.isOpenFullscreen)
	const isOpenMenuCollapseAnimation = ref(defaultSystemSetting.isOpenMenuCollapseAnimation)
	const isOpenMenuUniqueOpened = ref(defaultSystemSetting.isOpenMenuUniqueOpened)

	return {
		isOpenBreadcrumb,
		isOpenBreadcrumbIcon,
		isOpenTag,
		isOpenTagIcon,
		isOpenTagCache,
		isOpenDynamicTitle,
		isOpenLogo,
		isOpenProgress,
		isOpenCache,
		isOpenMenuSearch,
		isOpenRefreshPage,
		isOpenLayoutSetting,
		isOpenFullscreen,
		isOpenMenuCollapseAnimation,
		isOpenMenuUniqueOpened
	}
})
