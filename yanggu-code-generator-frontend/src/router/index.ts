import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { appStore } from '@/store'

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
		meta: {
			title: '代码生成器',
			icon: 'icon-appstore'
		},
		children: [
			{
				path: '/gen/project',
				name: 'Project',
				component: () => import('@/views/project/index.vue'),
				meta: {
					title: '项目管理',
					icon: 'icon-edit-square'
				}
			},
			{
				path: '/gen/table',
				name: 'Table',
				component: () => import('@/views/table/index.vue'),
				meta: {
					title: '表管理',
					icon: 'icon-detail'
				}
			},
			{
				path: '/gen/enum',
				name: 'Enum',
				component: () => import('@/views/enum/index.vue'),
				meta: {
					title: '枚举管理',
					icon: 'icon-merge-cells'
				}
			},
			{
				path: '/gen/template-group',
				name: 'TemplateGroup',
				component: () => import('@/views/template-group/index.vue'),
				meta: {
					title: '模板组管理',
					icon: 'icon-file-fill'
				}
			},
			{
				path: '/gen/datasource',
				name: 'DataSource',
				component: () => import('@/views/datasource/index.vue'),
				meta: {
					title: '数据源管理',
					icon: 'icon-database-fill'
				}
			},
			{
				path: '/gen/base-class',
				name: 'BaseClass',
				component: () => import('@/views/base-class/index.vue'),
				meta: {
					title: '基类管理',
					icon: 'icon-cluster'
				}
			},
			{
				path: '/gen/field-type',
				name: 'FieldType',
				component: () => import('@/views/field-type/index.vue'),
				meta: {
					title: '字段类型管理',
					icon: 'icon-menu'
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

// 路由拦截
router.beforeEach((to, from, next) => {
	console.log(to, from)

	const store = appStore()

	const routeMetaData = { name: to.meta.title, fullPath: to.fullPath }
	// 添加标签
	store.addTag(routeMetaData)

	// 设置面包屑
	store.setBreadcrumb(routeMetaData)

	next()
})
