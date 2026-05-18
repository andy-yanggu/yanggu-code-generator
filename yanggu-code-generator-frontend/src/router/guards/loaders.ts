import { RouteRecordRaw } from 'vue-router'
import type { MenuInfo } from '@/types'

/**
 * 动态组件加载器
 * 使用映射表优化性能，避免每次遍历所有模块
 */

// 预加载所有视图组件，构建映射表
const viewModules = import.meta.glob('@/views/**/*.vue')
const iframeModules = import.meta.glob('@/components/router/iframe-page.vue')

// 构建组件路径映射表（只执行一次）
const viewModuleMap = new Map<string, () => Promise<unknown>>()
for (const [path, moduleLoader] of Object.entries(viewModules)) {
	// 提取组件路径：@/views/system/user/index.vue -> system/user/index
	const componentPath = path.split('views/')[1]?.replace('.vue', '')
	if (componentPath) {
		viewModuleMap.set(componentPath, moduleLoader)
	}
}

/**
 * 加载视图组件
 * @param component 组件路径（相对于 views 目录）
 * @returns 组件加载函数
 */
export const loadView = (component: string | undefined): (() => Promise<Record<string, unknown>>) => {
	if (!component) {
		console.warn('组件路径为空，加载 404 页面')
		return () => import('@/views/error/404.vue')
	}

	// 从映射表中查找，O(1) 复杂度
	const moduleLoader = viewModuleMap.get(component)
	if (moduleLoader) {
		return moduleLoader as () => Promise<Record<string, unknown>>
	}

	// 未找到组件，警告并返回 404
	console.warn(`未找到组件: ${component}，加载 404 页面`)
	return () => import('@/views/error/404.vue')
}

/**
 * 加载 iframe 组件
 * @param item 菜单信息
 * @returns 组件加载函数
 */
export const loadIframeComponent = (item: MenuInfo): (() => Promise<Record<string, unknown>>) => {
	// 如果有自定义组件，则加载自定义组件
	if (item.component) {
		return loadView(item.component)
	}

	// 使用公共 iframe 组件，并动态设置 name
	const iframeLoader = Object.values(iframeModules)[0]
	if (!iframeLoader) {
		console.warn('未找到 iframe 组件')
		return () => import('@/views/error/404.vue')
	}

	return () =>
		iframeLoader().then((comp: any) => {
			const component = comp.default as { name?: string }
			component.name = item.name
			return comp
		})
}

/**
 * 构建路由列表
 * @param menuList 菜单列表
 * @returns 路由配置数组
 */
export const buildRouteList = (menuList: MenuInfo[]): RouteRecordRaw[] => {
	return menuList
		.map(item => {
			// 处理外链菜单（iframe 内嵌）
			if (item.meta.type === 3) {
				return {
					path: item.path,
					name: item.name,
					component: loadIframeComponent(item),
					props: { iframeSrc: item.meta.externalUrl, cache: item.meta.cache },
					meta: {
						...item.meta
					}
				} as RouteRecordRaw
			}

			// 新窗口打开方式，不创建路由
			if (item.meta.type === 4) {
				return null
			}

			const menu = { ...item } as RouteRecordRaw

			// 如果是目录，递归构建子路由
			if (item.meta.type === 0 && item.children && item.children.length > 0) {
				menu.children = buildRouteList(item.children)
			}

			// 如果是菜单，加载对应组件
			if (item.meta.type === 1) {
				menu.component = loadView(item.component)
			}

			return menu
		})
		.filter(Boolean) as RouteRecordRaw[]
}
