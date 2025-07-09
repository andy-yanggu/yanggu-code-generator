import { defineStore } from 'pinia'
import { ref, computed, ComputedRef } from 'vue'
import { menuRoutes } from '@/router'
import { RouteRecordRaw } from 'vue-router'

export const appStore = defineStore('app', () => {
	// 数据
	const isCollapseRef = ref(false)
	const tagsListRef = ref<{ fullPath: string; name: string }[]>([])
	const breadcrumbListRef = ref<{ path: string; name: string }[]>([])

	// 计算属性
	const tagLength = computed<number>(() => tagsListRef.value.length)

	// actions
	const toggleCollapse = () => {
		isCollapseRef.value = !isCollapseRef.value
	}

	const setBreadcrumb = (breadcrumb: any) => {
		const matched: { path: string; name: string }[] = []
		const fullPath = breadcrumb.fullPath
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

	const addTag = (tag: any) => {
		const isExist = tagsListRef.value.find(item => item.fullPath === tag.fullPath)
		const includes = tag.fullPath.includes('redirect')
		if (!isExist && !includes) {
			tagsListRef.value.push(tag)
		}
	}

	const removeTag = (tag: any) => {
		tagsListRef.value = tagsListRef.value.filter(item => item.fullPath !== tag.fullPath)
	}

	const addAllTags = (tagList: any[]) => {
		tagsListRef.value = tagList
	}

	return {
		isCollapseRef,
		breadcrumbListRef,
		tagsListRef,
		tagLength,
		toggleCollapse,
		setBreadcrumb,
		addTag,
		removeTag,
		addAllTags
	}
})

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
