import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { menuRoutes } from '@/router'
import { RouteRecordRaw } from 'vue-router'

// 标签数据
export interface Tag {
	// 完整路径
	fullPath: string
	// 组件名称
	name: string
	// 标题
	title: string
	// 图标
	icon: string
}

export const appStore = defineStore(
	'app',
	() => {
		// 状态
		// 折叠状态
		const isCollapseRef = ref(false)
		// 标签列表
		const tagsListRef = ref<Tag[]>([])
		// 面包屑列表
		const breadcrumbListRef = ref<{ path: string; name: string }[]>([])
		// 缓存组件列表
		const cacheListRef = ref<string[]>([])

		// 计算属性
		// 标签数量
		const tagLength = computed<number>(() => tagsListRef.value.length)

		// actions
		// 切换折叠状态
		const toggleCollapse = () => {
			isCollapseRef.value = !isCollapseRef.value
		}

		// 设置面包屑
		const setBreadcrumb = (routeMetaData: any) => {
			const matched: { path: string; name: string }[] = []
			const fullPath = routeMetaData.fullPath
			const paths = fullPath.split('/').filter((p: any) => p)

			let currentPath = ''
			for (const path of paths) {
				currentPath += `/${path}`
				const routeMeta = findRouteByPath(currentPath)
				if (routeMeta) {
					matched.push({
						path: currentPath,
						name: routeMeta
					})
				}
			}
			breadcrumbListRef.value = matched
		}

		// 添加标签
		const addTag = (routeMetaData: Tag) => {
			const isExist = tagsListRef.value.find(item => item.fullPath === routeMetaData.fullPath)
			const includes = routeMetaData.fullPath.includes('redirect')
			if (!isExist && !includes) {
				tagsListRef.value.push(routeMetaData)
			}
		}

		// 删除标签
		const removeTag = (tag: Tag) => {
			tagsListRef.value = tagsListRef.value.filter(item => item.fullPath !== tag.fullPath)
		}

		// 添加所有标签
		const addAllTags = (tagList: Tag[]) => {
			tagsListRef.value = tagList
		}

		// 删除所有标签
		const removeAllTags = () => {
			tagsListRef.value = []
		}

		// 添加缓存路由
		const addCacheComponent = (name: string) => {
			if (!cacheListRef.value.includes(name)) {
				cacheListRef.value.push(name)
			}
		}

		// 删除缓存路由
		const removeCacheComponent = (name: string) => {
			cacheListRef.value = cacheListRef.value.filter(item => item !== name)
		}

		return {
			isCollapseRef,
			breadcrumbListRef,
			tagsListRef,
			tagLength,
			cacheListRef,
			toggleCollapse,
			setBreadcrumb,
			addTag,
			removeTag,
			addAllTags,
			removeAllTags,
			addCacheComponent,
			removeCacheComponent
		}
	},
	{
		persist: {
			storage: localStorage,
			key: 'appStore'
		}
	}
)

/**
 * 递归查找路径对应的 meta.title
 */
const findRouteByPath = (targetPath: string): string | null => {
	const traverse = (routes: RouteRecordRaw[]): string | null => {
		for (const route of routes) {
			// 完全匹配路径
			if (route.path === targetPath && route.meta?.title) {
				return route.meta.title as string
			}

			// 存在子路由则继续递归
			if (route.children?.length) {
				const result = traverse(route.children)
				if (result) {
					return result
				}
			}
		}
		return null
	}

	return traverse(menuRoutes)
}
