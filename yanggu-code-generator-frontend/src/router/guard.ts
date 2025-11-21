import { useAppStore, useSystemSettingStore, useUserStore } from '@/store'
import { Router, RouteRecordRaw } from 'vue-router'
import 'nprogress/nprogress.css'
import NProgress from 'nprogress'
import { setTitle } from '@/utils/tool'
import { MenuInfo, RouteMetaData } from '@/types'

NProgress.configure({ showSpinner: false })

// 路由白名单名称列表
const whiteLists: string[] = ['AuthLogin']

export const routeGuard = (router: Router) => {
	// 路由前置拦截
	router.beforeEach((to, from, next) => {
		const systemSettingStore = useSystemSettingStore()

		if (systemSettingStore.isOpenProgress) {
			NProgress.start()
		}
		const routeMetaData: RouteMetaData = {
			name: to.name as string,
			path: to.path,
			fullPath: to.fullPath,
			title: to.meta?.title as string,
			icon: to.meta?.icon as string,
			cache: to.meta?.cache as boolean,
			type: to.meta?.type as number,
			hidden: to.meta?.hidden as boolean,
			externalUrl: to.meta?.externalUrl as string
		}

		// console.log(to, from, routeMetaData)

		// 如果包含白名单，直接放行
		if (whiteLists.includes(routeMetaData.name)) {
			next()
			return
		}

		const userStore = useUserStore()
		// 是否登录
		// if (!userStore.isLogin) {
		// 	// 未登录，跳转登录页面
		// 	next({
		// 		name: 'Login',
		// 		query: {
		// 			redirect: encodeURIComponent(to.fullPath || '/')
		// 		}
		// 	})
		// 	NProgress.done()
		// 	return
		// }
		// 是否添加路由
		if (!userStore.isAddRoutes) {
			// 1. 添加路由
			const routeList = buildRouteList(userStore.menuList)
			routeList.forEach((item: RouteRecordRaw) => {
				// console.log('item', item)
				router.addRoute('Layout', item)
			})

			// 3. 设置添加路由标记
			userStore.setAddRouteFlag()
			// 添加完路由需要重新执行一次路由跳转，否则会出现空白页面
			next({ ...to, replace: true })
			return
		}

		// 动态设置默认跳转
		if (to.path === '/') {
			return next(systemSettingStore.menuDefault ?? '/index')
		}

		const appStore = useAppStore()

		// 不是新窗口,添加tag标签
		if (routeMetaData.type !== 4 && systemSettingStore.isOpenTag) {
			// 添加标签
			appStore.addTag({
				...routeMetaData
			})
		}

		// 添加缓存路由 - 有名称和是菜单时才添加缓存
		if (routeMetaData.cache && routeMetaData.name && routeMetaData.type === 1 && systemSettingStore.isOpenPageCache) {
			appStore.addCacheComponent(routeMetaData.name)
		}

		next()
	})

	// 路由后置拦截
	router.afterEach(to => {
		const systemSettingStore = useSystemSettingStore()
		// 设置动态标题
		if (systemSettingStore.isOpenDynamicTitle) {
			setTitle(to.meta.title as string)
		}

		// 关闭进度条
		if (systemSettingStore.isOpenProgress && NProgress.isStarted()) {
			NProgress.done()
		}
	})
}

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('@/views/**/*.vue')
const loadView = (component: string | undefined) => {
	let res
	for (const path in modules) {
		const dir = path.split('/src/views/')[1].split('.vue')[0]
		if (dir === component) {
			res = () => modules[path]()
			return res
		}
	}
	if (!res) {
		console.warn(`未找到组件: ${component}`)
		res = () => import('@/views/error/404.vue')
		return res
	}
}

// 构建路由
const buildRouteList = (menuList: MenuInfo[]): RouteRecordRaw[] => {
	return menuList
		.map((item: MenuInfo) => {
			// 处理外链菜单
			if (item.meta.type === 3) {
				// iframe内嵌方式
				return {
					path: item.path,
					name: item.name,
					component: loadIframeComponent(item),
					props: { iframeSrc: item.meta.externalUrl, cache: item.meta.cache },
					meta: {
						...item.meta
					}
				} as RouteRecordRaw
			} else if (item.meta.type === 4) {
				// 新窗口打开方式，创建一个跳转页面
				return null
			}

			const menu = {
				...item
			} as RouteRecordRaw

			// 如果是目录，则需要构建子路由
			if (item.meta.type === 0 && item.children && item.children.length > 0) {
				menu.children = buildRouteList(item.children)
			}
			// 如果是菜单，则需要构建组件
			if (item.meta.type === 1) {
				menu.component = loadView(item.component)
			}
			return menu
		})
		.filter(Boolean) as RouteRecordRaw[]
}

// 处理iframe菜单对应的组件
const loadIframeComponent = (item: MenuInfo) => {
	// 如果有组件，则直接加载组件
	if (item.component) {
		return loadView(item.component)
	} else {
		// 否则使用公共的组件，并且重新设置组件名称
		return () =>
			import('@/components/router/iframe-page.vue').then(comp => {
				// 给组件动态设置 name
				comp.default.name = item.name
				return comp
			})
	}
}
