import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { appStore, Tag } from '@/store'

export const menuRoutes: RouteRecordRaw[] = [
	{
		path: '/index',
		component: () => import('@/views/index.vue'),
		meta: {
			title: '首页',
			icon: 'icon-detail'
		}
	},
	{
		path: '/gen',
		name: 'Gen',
		meta: {
			name: 'Gen',
			title: '代码生成器',
			icon: 'icon-appstore'
		},
		children: [
			{
				path: '/gen/project',
				name: 'GenProject',
				component: () => import('@/views/project/index.vue'),
				meta: {
					name: 'GenProject',
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
					name: 'GenTable',
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
					name: 'GenEnum',
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
					name: 'GenTemplateGroup',
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
					name: 'GenDatasource',
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
					name: 'GenBaseClass',
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
					name: 'GenFieldType',
					title: '字段类型管理',
					icon: 'icon-menu',
					cache: true
				}
			}
		]
	}
]

export const constantRoutes: RouteRecordRaw[] = [
	{
		path: '/redirect',
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

// 路由拦截
router.beforeEach((to, from, next) => {
	const store = appStore()

	const meta = to.meta

	// console.log(to, from)
	const routeMetaData: Tag = {
		title: meta.title as string,
		fullPath: to.fullPath,
		name: meta.name as string,
		icon: meta.icon as string
	}
	// console.log(routeMetaData)
	// 添加标签
	if (meta.title) {
		store.addTag(routeMetaData)
	}

	// 设置面包屑
	store.setBreadcrumb(routeMetaData)

	// 添加缓存路由
	if (meta.cache && meta.name) {
		store.addCacheComponent(meta.name as string)
	}

	next()
})
