import { RouteLocationNormalized, Router } from 'vue-router'
import { useAppStore, useSystemSettingStore } from '@/store'
import { setTitle } from '@/utils/tool'
import { RouteMetaData } from '@/types'
import { useTimeoutFn } from '@vueuse/core'
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
 * 提取路由元数据
 */
const extractRouteMetaData = (to: RouteLocationNormalized): RouteMetaData => {
	return {
		name: to.name as string,
		path: to.path,
		fullPath: to.fullPath,
		title: (to.meta?.title as string) || '',
		icon: (to.meta?.icon as string) || '',
		cache: (to.meta?.cache as boolean) || false,
		type: (to.meta?.type as number) || 0,
		hidden: (to.meta?.hidden as boolean) || false,
		externalUrl: (to.meta?.externalUrl as string) || ''
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
	const appStore = useAppStore()
	const systemSettingStore = useSystemSettingStore()

	// 不是新窗口且标签页功能开启时，添加标签
	if (routeMeta.type !== 4 && systemSettingStore.tag.isOpenTag) {
		appStore.addTag({
			...routeMeta,
			pinned: false
		})
	}
}

/**
 * 处理页面缓存
 */
const handlePageCache = (routeMeta: RouteMetaData): void => {
	const appStore = useAppStore()
	const systemSettingStore = useSystemSettingStore()

	// 有名称、是菜单、需要缓存且页面缓存功能开启时，添加到缓存
	if (routeMeta.cache && routeMeta.name && routeMeta.type === 1 && systemSettingStore.other.isOpenPageCache) {
		appStore.addCacheComponent(routeMeta.name)
	}
}

/**
 * 注册前置守卫（UI 相关）
 */
export const registerUIBeforeGuard = (router: Router): void => {
	router.beforeEach((to, _from, next) => {
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

		next()
	})
}

/**
 * 注册后置守卫（UI 相关）
 */
export const registerUIAfterGuard = (router: Router): void => {
	router.afterEach(to => {
		const systemSettingStore = useSystemSettingStore()

		// 设置动态标题
		if (systemSettingStore.other.isOpenDynamicTitle) {
			setTitle((to.meta.title as string) || '')
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
