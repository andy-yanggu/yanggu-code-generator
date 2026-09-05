import { RouteRecordRaw } from 'vue-router'

/**
 * 静态路由配置
 * 包含：布局、认证、用户中心、重定向、错误页等基础路由
 */
export const constantRoutes: RouteRecordRaw[] = [
	// 根路径 - 主布局
	{
		path: '/',
		name: 'Layout',
		component: () => import('@/layout/index.vue'),
		meta: {
			title: '首页',
			icon: 'icon-home',
			hideTab: true,
			type: 1
		}
	},
	// 认证模块（登录/注册）
	{
		path: '/auth',
		name: 'Auth',
		children: [
			{
				path: 'login',
				name: 'AuthLogin',
				component: () => import('@/views/auth/login.vue'),
				meta: {
					title: '登录',
					cache: false,
					hideMenu: true,
					hideTab: true,
					type: 1
				}
			},
			{
				path: 'register',
				name: 'AuthRegister',
				component: () => import('@/views/auth/register.vue'),
				meta: {
					title: '注册',
					cache: false,
					hideMenu: true,
					hideTab: true,
					type: 1
				}
			}
		]
	},
	// 用户中心
	{
		path: '/user',
		name: 'User',
		component: () => import('@/layout/index.vue'),
		meta: {
			title: '个人中心',
			icon: 'icon-user',
			type: 0
		},
		children: [
			{
				path: 'profile',
				name: 'UserProfile',
				component: () => import('@/views/user/profile.vue'),
				meta: {
					title: '个人信息',
					icon: 'icon-user-profile',
					hideMenu: true,
					cache: false,
					type: 1
				}
			}
		]
	},
	// 重定向路由（用于刷新页面）
	{
		path: '/redirect',
		name: 'Redirect',
		component: () => import('@/layout/index.vue'),
		children: [
			{
				path: ':path(.*)',
				component: () => import('@/components/router/redirect.vue'),
				name: 'RouterRedirect',
				meta: {
					title: '重定向',
					cache: false,
					hideTab: true,
					type: 1
				}
			}
		]
	},
	// 错误页面集合
	{
		path: '/error',
		name: 'Error',
		component: () => import('@/layout/index.vue'),
		meta: {
			title: '错误页',
			icon: 'icon-error',
			type: 0
		},
		children: [
			{
				path: '400',
				name: 'Error400',
				component: () => import('@/views/error/400.vue'),
				meta: {
					title: '400',
					icon: 'icon-error-400',
					cache: false,
					hideTab: true,
					type: 1
				}
			},
			{
				path: '401',
				name: 'Error401',
				component: () => import('@/views/error/401.vue'),
				meta: {
					title: '401',
					icon: 'icon-error-401',
					cache: false,
					hideTab: true,
					type: 1
				}
			},
			{
				path: '403',
				name: 'Error403',
				component: () => import('@/views/error/403.vue'),
				meta: {
					title: '403',
					icon: 'icon-error-403',
					cache: false,
					hideTab: true,
					type: 1
				}
			},
			{
				path: '404',
				name: 'Error404',
				component: () => import('@/views/error/404.vue'),
				meta: {
					title: '404',
					icon: 'icon-error-404',
					cache: false,
					hideTab: true,
					type: 1
				}
			},
			{
				path: '500',
				name: 'Error500',
				component: () => import('@/views/error/500.vue'),
				meta: {
					title: '500',
					icon: 'icon-error-500',
					cache: false,
					hideTab: true,
					type: 1
				}
			}
		]
	},
	// 404 兜底路由（必须放在最后）
	{
		path: '/:pathMatch(.*)*',
		component: () => import('@/layout/index.vue'),
		children: [
			{
				path: '',
				component: () => import('@/views/error/404.vue'),
				meta: {
					title: '404',
					icon: 'icon-error-404',
					cache: false,
					hideTab: true,
					type: 1
				}
			}
		]
	}
]
