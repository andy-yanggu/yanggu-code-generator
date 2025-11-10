import { RouteRecordRaw } from 'vue-router'

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
		path: '/login',
		name: 'Login',
		component: () => import('@/views/auth/login.vue'),
		meta: {
			title: '登录',
			cache: false,
			type: 1
		}
	},
	{
		path: '/register',
		name: 'Register',
		component: () => import('@/views/auth/register.vue'),
		meta: {
			title: '注册',
			cache: false,
			type: 1
		}
	},
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
				path: '/user/profile',
				name: 'UserProfile',
				component: () => import('@/views/user/profile.vue'),
				meta: {
					title: '个人信息',
					icon: 'icon-user-profile',
					hidden: true,
					cache: false,
					type: 1
				}
			}
		]
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
					cache: false,
					type: 1
				}
			}
		]
	},
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
				path: '/error/400',
				name: 'Error400',
				component: () => import('@/views/error/400.vue'),
				meta: {
					title: '400',
					icon: 'icon-error-400',
					cache: false,
					type: 1
				}
			},
			{
				path: '/error/401',
				name: 'Error401',
				component: () => import('@/views/error/401.vue'),
				meta: {
					title: '401',
					icon: 'icon-error-401',
					cache: false,
					type: 1
				}
			},
			{
				path: '/error/403',
				name: 'Error403',
				component: () => import('@/views/error/403.vue'),
				meta: {
					title: '403',
					icon: 'icon-error-403',
					cache: false,
					type: 1
				}
			},
			{
				path: '/error/404',
				name: 'Error404',
				component: () => import('@/views/error/404.vue'),
				meta: {
					title: '404',
					icon: 'icon-error-404',
					cache: false,
					type: 1
				}
			},
			{
				path: '/error/500',
				name: 'Error500',
				component: () => import('@/views/error/500.vue'),
				meta: {
					title: '500',
					icon: 'icon-error-500',
					cache: false,
					type: 1
				}
			}
		]
	},
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
					type: 1
				}
			}
		]
	}
]
