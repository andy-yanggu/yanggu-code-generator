import { RouteLocationNormalized, Router } from 'vue-router'
import { useAppStore, useCacheStore, useMenuPreferenceStore, useSystemSettingStore, useTagStore } from '@/store'
import { setTitle } from '@/utils/tool'
import { RouteMetaData } from '@/types'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { DEFAULT_HOME_PATH, PROGRESS_DELAY } from '@/config/router'

/**
 * UI 相关的路由守卫
 * 负责：进度条、标题、标签页、页面缓存
 */

// 配置 NProgress
NProgress.configure({ showSpinner: false })

/**
 * 提取路由元数据（只取最深层子路由的 meta，避免父路由 meta 传播）
 */
const extractRouteMetaData = (to: RouteLocationNormalized): RouteMetaData => {
	const meta = to.matched.at(-1)?.meta ?? ({} as RouteMetaData)
	return {
		name: to.name as string,
		path: to.path,
		fullPath: to.fullPath,
		title: (meta.title as string) || '',
		icon: (meta.icon as string) || '',
		cache: (meta.cache as boolean) || false,
		type: (meta.type as number) || 0,
		hideMenu: (meta.hideMenu as boolean) || false,
		hideTab: (meta.hideTab as boolean) || false,
		externalUrl: (meta.externalUrl as string) || ''
	}
}

/**
 * 处理进度条启动
 */
const handleProgressStart = (): void => {
	const systemSettingStore = useSystemSettingStore()
	if (systemSettingStore.other.isOpenProgress) {
		NProgress.start()
	}
}

/**
 * 处理标签页添加
 */
const handleTagAdd = (routeMeta: RouteMetaData): void => {
	const tagStore = useTagStore()
	const systemSettingStore = useSystemSettingStore()
	const menuPreferenceStore = useMenuPreferenceStore()

	// 使用用户偏好覆盖的 hideTab 值
	const effectiveHideTab = menuPreferenceStore.getEffectiveHideTab(routeMeta.path, routeMeta.hideTab)

	// 隐藏标签、新窗口、标签页功能未开启时，不添加标签
	if (!effectiveHideTab && routeMeta.type !== 4 && systemSettingStore.tag.isOpenTag) {
		const effectiveTitle = menuPreferenceStore.getEffectiveTitle(routeMeta.path, routeMeta.title)
		tagStore.addTag({
			...routeMeta,
			title: effectiveTitle,
			pinned: false
		})
	}
}

/**
 * 处理页面缓存
 */
const handlePageCache = (routeMeta: RouteMetaData): void => {
	const cacheStore = useCacheStore()
	const systemSettingStore = useSystemSettingStore()
	const menuPreferenceStore = useMenuPreferenceStore()

	// 使用用户偏好覆盖的 cache 值
	const effectiveCache = menuPreferenceStore.getEffectiveCache(routeMeta.path, routeMeta.cache)

	// 有名称、是菜单、需要缓存且页面缓存功能开启时，添加到缓存
	if (effectiveCache && routeMeta.name && routeMeta.type === 1 && systemSettingStore.other.isOpenPageCache) {
		cacheStore.addCacheComponent(routeMeta.name)
	}
}

/**
 * 注册前置守卫（UI 相关）
 */
export const registerUIBeforeGuard = (router: Router): void => {
	router.beforeEach(to => {
		const appStore = useAppStore()

		// 设置全局加载状态
		appStore.globalLoading = true

		// 启动进度条
		handleProgressStart()

		// 提取路由元数据
		const routeMeta = extractRouteMetaData(to)

		// 添加标签页
		handleTagAdd(routeMeta)

		// 处理页面缓存
		handlePageCache(routeMeta)
	})
}

/**
 * 注册后置守卫（UI 相关）
 */
export const registerUIAfterGuard = (router: Router): void => {
	router.afterEach(to => {
		const systemSettingStore = useSystemSettingStore()
		const menuPreferenceStore = useMenuPreferenceStore()

		// 设置动态标题（只取最深层子路由的 meta）
		if (systemSettingStore.other.isOpenDynamicTitle) {
			const meta = to.matched.at(-1)?.meta ?? ({} as RouteMetaData)
			const serverTitle = (meta.title as string) || ''
			const effectiveTitle = menuPreferenceStore.getEffectiveTitle(to.path, serverTitle)
			setTitle(effectiveTitle)
		}

		// 延迟关闭全局加载状态
		const appStore = useAppStore()
		useTimeoutFn(() => {
			appStore.globalLoading = false
		}, PROGRESS_DELAY)

		// 关闭进度条
		if (systemSettingStore.other.isOpenProgress && NProgress.isStarted()) {
			NProgress.done()
		}
	})
}

/**
 * 处理默认首页跳转
 */
export const handleDefaultRedirect = (to: RouteLocationNormalized): string | null => {
	if (to.path === '/') {
		const systemSettingStore = useSystemSettingStore()
		return systemSettingStore.menu.menuDefault || DEFAULT_HOME_PATH
	}
	return null
}
