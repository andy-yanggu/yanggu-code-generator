import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { routerGuard } from '@/utils/router-guard'

// 常量路由
export const constantRoutes: RouteRecordRaw[] = [
	{
		path: '/',
		name: 'Layout',
		component: () => import('@/layout/index.vue'),
		redirect: '/index',
		children: []
	},
	{
		path: '/redirect',
		name: 'Redirect',
		component: () => import('@/layout/index.vue'),
		children: [
			{
				path: '/redirect/:path(.*)',
				component: () => import('@/views/router/redirect.vue'),
				meta: {
					title: '重定向',
					cache: false
				}
			}
		]
	},
	{
		path: '/:pathMatch(.*)',
		component: () => import('@/views/404.vue')
	}
]

export const router = createRouter({
	history: createWebHashHistory(),
	scrollBehavior(_to, _from, savedPosition) {
		if (savedPosition) {
			return savedPosition
		} else {
			return { top: 0 }
		}
	},
	routes: constantRoutes
})

// 路由拦截
routerGuard(router)
