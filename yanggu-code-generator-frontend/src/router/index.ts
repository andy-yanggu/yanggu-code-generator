import { createRouter, createWebHashHistory } from 'vue-router'
import { routeGuard } from '@/router/guards'
import { constantRoutes } from '@/router/constant'

/**
 * 路由实例配置
 *
 * 设计说明：
 * - 使用 Hash 模式（createWebHashHistory），兼容性更好
 * - 滚动行为：恢复保存的位置或回到顶部
 * - 路由守卫：在创建后立即注册
 */
export const router = createRouter({
	history: createWebHashHistory(),
	// 滚动行为控制
	scrollBehavior(_to, _from, savedPosition) {
		// 如果有保存的位置（浏览器前进/后退），使用保存的位置
		if (savedPosition) {
			return savedPosition
		}
		// 否则滚动到顶部
		return { top: 0 }
	},
	// 注册静态路由（动态路由在守卫中按需添加）
	routes: constantRoutes
})

// 路由拦截
routeGuard(router)
