import { RouteLocationNormalized, Router, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store'
import type { MenuInfo } from '@/types'
import { buildRouteList } from './loaders'
import { DEFAULT_LAYOUT_NAME, ENABLE_AUTH_CHECK, ROUTE_WHITE_LIST } from '@/router/config'
import { isNotEmpty } from '@/utils/tool'

/**
 * 权限相关的路由守卫
 * 负责：登录校验、动态路由加载
 */

/**
 * 检查路由是否在白名单中
 */
const isInWhiteList = (routeName: string | symbol | undefined | null): boolean => {
	if (!routeName) {
		return false
	}
	return ROUTE_WHITE_LIST.includes(routeName as string)
}

/**
 * 添加动态路由到路由器
 */
const addDynamicRoutes = (router: Router, menuList: MenuInfo[]): void => {
	const routeList = buildRouteList(menuList)

	// 递归添加路由到对应的父级路由下
	const addRoutes = (routes: RouteRecordRaw[], parentName: string = DEFAULT_LAYOUT_NAME) => {
		routes.forEach(route => {
			router.addRoute(parentName, route)

			// 递归添加子路由
			if (isNotEmpty(route.children)) {
				addRoutes(route.children as RouteRecordRaw[], route.name as string)
			}
		})
	}

	addRoutes(routeList)
}

/**
 * 处理登录校验
 */
const handleAuthCheck = (
	to: RouteLocationNormalized,
	userStore: ReturnType<typeof useUserStore>
): { shouldRedirect: boolean; redirectPath?: string } => {
	// 如果未启用登录校验，直接跳过
	if (!ENABLE_AUTH_CHECK) {
		return { shouldRedirect: false }
	}

	// 检查是否登录
	if (!userStore.isLogin) {
		return {
			shouldRedirect: true,
			redirectPath: `/auth/login?redirect=${encodeURIComponent(to.fullPath || '/')}`
		}
	}

	return { shouldRedirect: false }
}

/**
 * 处理动态路由加载
 */
const handleDynamicRoutes = async (
	_to: RouteLocationNormalized,
	router: Router,
	userStore: ReturnType<typeof useUserStore>
): Promise<{ shouldReplace: boolean }> => {
	// 如果已经添加过路由，跳过
	if (userStore.isAddRoutes) {
		return { shouldReplace: false }
	}

	try {
		// 添加动态路由
		addDynamicRoutes(router, userStore.menuList)

		// 设置已添加路由标记
		userStore.setAddRouteFlag()

		// 需要重新执行路由跳转
		return { shouldReplace: true }
	} catch (error) {
		console.error('动态路由加载失败:', error)
		// 路由加载失败，跳转到错误页面
		router.replace('/error/500')
		return { shouldReplace: false }
	}
}

/**
 * 注册权限守卫
 */
export const registerAuthGuard = (router: Router): void => {
	router.beforeEach(async (to, _from, next) => {
		const userStore = useUserStore()

		// 1. 检查白名单
		if (isInWhiteList(to.name)) {
			next()
			return
		}

		// 2. 登录校验
		const authResult = handleAuthCheck(to, userStore)
		if (authResult.shouldRedirect && authResult.redirectPath) {
			next(authResult.redirectPath)
			return
		}

		// 3. 动态路由加载
		const routeResult = await handleDynamicRoutes(to, router, userStore)
		if (routeResult.shouldReplace) {
			next({ ...to, replace: true })
			return
		}

		// 4. 放行
		next()
	})
}
