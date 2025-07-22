import { useAppStore, NavbarTag } from '@/store/app-store'
import { Router } from 'vue-router'

// 路由数据
export interface RouteMetaData {
	// 完整路径
	fullPath: string
	// 组件名称
	name: string
	// 路径
	path: string
	// 标题
	title: string
	// 图标
	icon: string
	// 缓存
	cache: boolean
}

export const routerGuard = (router: Router) => {
	// 路由拦截
	router.beforeEach((to, from, next) => {
		const appStore = useAppStore()
		const meta = to.meta

		const routeMetaData: RouteMetaData = {
			name: to.name as string,
			title: meta?.title as string,
			fullPath: to.fullPath,
			path: to.path,
			icon: meta?.icon as string,
			cache: meta?.cache as boolean
		}
		console.log(to, from, routeMetaData)
		// 添加标签
		if (routeMetaData.title) {
			const tag: NavbarTag = {
				...routeMetaData
			}
			appStore.addTag(tag)
		}

		// 设置面包屑
		appStore.setBreadcrumb(routeMetaData)

		// 添加缓存路由
		if (routeMetaData.cache && routeMetaData.name) {
			appStore.addCacheComponent(routeMetaData.name)
		}

		next()
	})
}
