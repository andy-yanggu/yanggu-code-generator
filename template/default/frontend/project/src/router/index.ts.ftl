import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'

export const menuRoutes: RouteRecordRaw[] = [
	{
		path: '/gen/project',
		name: 'Project',
		component: () => import('@/views/project/index.vue'),
		meta: {
			title: '项目管理',
			icon: 'icon-edit-square'
		}
	}
]

export const constantRoutes: RouteRecordRaw[] = [
	{
		path: '/redirect',
		component: () => import('@/layout/index.vue'),
		children: [
			{
				path: '/redirect/:path(.*)',
				component: () => import('@/layout/components/router/redirect.vue')
			}
		]
	},
	{
		path: '/',
		component: () => import('@/layout/index.vue'),
		redirect: '/gen/project',
		children: [...menuRoutes]
	},
	{
		path: '/404',
		component: () => import('@/views/404.vue')
	},
	{
		path: '/:pathMatch(.*)',
		redirect: '/404'
	}
]

export const router = createRouter({
	history: createWebHashHistory(),
	routes: constantRoutes
})
