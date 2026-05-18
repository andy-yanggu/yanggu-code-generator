import { Router } from 'vue-router'
import { registerAuthGuard } from './auth'
import { registerUIBeforeGuard, registerUIAfterGuard, handleDefaultRedirect } from './ui'

/**
 * 路由守卫统一入口
 * 整合所有守卫逻辑,按顺序注册
 * 执行顺序：
 * 1. 权限守卫（登录校验 → 动态路由加载）
 * 2. UI 前置守卫（进度条 → 标签页 → 缓存）
 * 3. UI 后置守卫（标题 → 进度条结束）
 * 4. 默认首页跳转
 */
export const routeGuard = (router: Router): void => {
	// 注册权限守卫（登录校验、动态路由）
	registerAuthGuard(router)

	// 注册 UI 前置守卫（进度条、标签、缓存）
	registerUIBeforeGuard(router)

	// 注册 UI 后置守卫（标题、进度条结束）
	registerUIAfterGuard(router)

	// 注册默认首页跳转逻辑（嵌入到 beforeEach）
	router.beforeEach((to, _from, next) => {
		const redirectPath = handleDefaultRedirect(to)
		if (redirectPath) {
			next(redirectPath)
			return
		}
		next()
	})
}
