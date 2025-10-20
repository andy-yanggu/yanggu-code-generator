import { RouteRecordRaw } from 'vue-router'

// 常量路由
export const constantRoutes: RouteRecordRaw[] = [
	{
		path: '/',
		name: 'Layout',
		component: () => import('@/layout/index.vue'),
		redirect: '/index',
		children: [
			{
				path: '/user/profile',
				name: 'Profile',
				component: () => import('@/views/user/profile.vue'),
				meta: {
					title: '个人中心',
					hidden: true,
					cache: false,
					type: 1
				}
			}
		]
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
		path: '/:pathMatch(.*)',
		component: () => import('@/views/error/404.vue'),
		meta: {
			title: '404',
			cache: false,
			type: 1
		}
	}
]
