import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { routerGuard } from '@/utils/router-guard'

export const menuRoutes: RouteRecordRaw[] = [
	{
		path: '/index',
		component: () => import('@/views/index.vue'),
		name: 'Index',
		meta: {
			title: '首页',
			icon: 'icon-detail'
		}
	},
	{
		path: '/gen',
		name: 'Gen',
		meta: {
			title: '代码生成器',
			icon: 'icon-appstore'
		},
		children: [
			{
				path: '/gen/project',
				name: 'GenProject',
				component: () => import('@/views/project/index.vue'),
				meta: {
					title: '项目管理',
					icon: 'icon-edit-square',
					cache: true
				}
			},
			{
				path: '/gen/table',
				name: 'GenTable',
				component: () => import('@/views/table/index.vue'),
				meta: {
					title: '表管理',
					icon: 'icon-detail',
					cache: true
				}
			},
			{
				path: '/gen/enum',
				name: 'GenEnum',
				component: () => import('@/views/enum/index.vue'),
				meta: {
					title: '枚举管理',
					icon: 'icon-merge-cells',
					cache: true
				}
			},
			{
				path: '/gen/template-group',
				name: 'GenTemplateGroup',
				component: () => import('@/views/template-group/index.vue'),
				meta: {
					title: '模板组管理',
					icon: 'icon-file-fill',
					cache: true
				}
			},
			{
				path: '/gen/datasource',
				name: 'GenDatasource',
				component: () => import('@/views/datasource/index.vue'),
				meta: {
					title: '数据源管理',
					icon: 'icon-database-fill',
					cache: true
				}
			},
			{
				path: '/gen/base-class',
				name: 'GenBaseClass',
				component: () => import('@/views/base-class/index.vue'),
				meta: {
					title: '基类管理',
					icon: 'icon-cluster',
					cache: true
				}
			},
			{
				path: '/gen/field-type',
				name: 'GenFieldType',
				component: () => import('@/views/field-type/index.vue'),
				meta: {
					title: '字段类型管理',
					icon: 'icon-menu',
					cache: true
				}
			}
		]
	}
]

// 常量路由
export const constantRoutes: RouteRecordRaw[] = [
	{
		path: '/',
		name: 'Layout',
		component: () => import('@/layout/index.vue'),
		redirect: '/gen/project',
		children: [...menuRoutes]
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

// 路由数据
export interface RouteMetaData {
	// 完整路径
	fullPath: string
	// 组件名称
	name: string
	// 标题
	title: string
	// 图标
	icon: string
	// 缓存
	cache: boolean
}

// 路由拦截
routerGuard(router)
