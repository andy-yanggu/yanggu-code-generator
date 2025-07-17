import { useAppStore, NavbarTag } from '@/store/use-app-store'
import { Router } from 'vue-router'
import { constantRoutes, RouteMetaData } from '@/router'

export const routerGuard = (router: Router) => {
	// 路由拦截
	router.beforeEach((to, from, next) => {
		const appStore = useAppStore()
		const meta = to.meta

		const routeMetaData: RouteMetaData = {
			name: to.name as string,
			title: meta?.title as string,
			fullPath: to.fullPath,
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
		appStore.setBreadcrumb(routeMetaData, constantRoutes)

		// 添加缓存路由
		if (routeMetaData.cache && routeMetaData.name) {
			appStore.addCacheComponent(routeMetaData.name)
		}

		next()
	})
}
