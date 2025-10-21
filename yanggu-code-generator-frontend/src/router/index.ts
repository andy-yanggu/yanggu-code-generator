import { createRouter, createWebHashHistory } from 'vue-router'
import { routeGuard } from '@/router/guard'
import { constantRoutes } from '@/router/constant'

// 创建路由实例
export const router = createRouter({
	history: createWebHashHistory(),
	scrollBehavior(_to, _from, savedPosition) {
		if (savedPosition) {
			return savedPosition
		} else {
			return { top: 0 }
		}
	},
	// 常量路由
	routes: constantRoutes
})

// 路由拦截
routeGuard(router)
