import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { RouteMetaData } from '@/utils/router-guard'
import { router } from '@/router'

// 标签数据
export interface NavbarTag {
	// 完整路径
	fullPath: string
	// 组件名称
	name: string
	// 标题
	title: string
	// 图标
	icon: string
}

// 面包屑
export interface Breadcrumb {
	// 标题
	title: string
	// 图标
	icon: string
}

export const useAppStore = defineStore(
	'app',
	() => {
		// 状态
		// 折叠状态
		const isCollapse = ref(false)
		// 标签列表
		const tagList = ref<NavbarTag[]>([])
		// 面包屑列表
		const breadcrumbList = ref<Breadcrumb[]>([])
		// 缓存组件列表
		const cacheList = ref<string[]>([])

		// 计算属性
		// 标签数量
		const tagLength = computed(() => tagList.value.length)

		// 缓存组件列表

		// actions
		// 切换折叠状态
		const toggleCollapse = () => {
			isCollapse.value = !isCollapse.value
		}

		// 设置面包屑
		const setBreadcrumb = (routeMetaData: RouteMetaData) => {
			const matched: Breadcrumb[] = []
			const paths = routeMetaData.path.split('/').filter((p: any) => p)

			let currentPath = ''
			for (const path of paths) {
				currentPath += `/<#noparse>${path}</#noparse>`
				const breadcrumb = findRouteByPath(currentPath)
				if (breadcrumb && breadcrumb.title && breadcrumb.icon) {
					matched.push(breadcrumb)
				}
			}
			breadcrumbList.value = matched
		}

		// 添加标签
		const addTag = (tag: NavbarTag) => {
			const isExist = tagList.value.find(item => item.fullPath === tag.fullPath)
			const includes = tag.fullPath.includes('redirect')
			if (!isExist && !includes) {
				tagList.value.push(tag)
			}
		}

		// 删除标签
		const removeTag = (tag: NavbarTag) => {
			tagList.value = tagList.value.filter(item => item.fullPath !== tag.fullPath)
		}

		// 添加所有标签
		const addAllTags = (tagList: NavbarTag[]) => {
			tagList.value = tagList
		}

		// 删除所有标签
		const removeAllTags = () => {
			tagList.value = []
		}

		// 添加缓存路由
		const addCacheComponent = (name: string) => {
			if (!cacheList.value.includes(name)) {
				cacheList.value.push(name)
			}
		}

		// 删除缓存路由
		const removeCacheComponent = (name: string) => {
			cacheList.value.splice(cacheList.value.indexOf(name), 1)
		}

		// 批量删除缓存路由
		const removeCacheComponentList = (nameList: string[]) => {
			if (!nameList || nameList.length === 0) {
				return
			}
			for (const name of nameList) {
				cacheList.value.splice(cacheList.value.indexOf(name), 1)
			}
		}

		// 删除所有缓存
		const removeAllCache = () => {
			cacheList.value = []
		}

		return {
			isCollapse,
			breadcrumbList,
			tagList,
			cacheList,
			tagLength,
			toggleCollapse,
			setBreadcrumb,
			addTag,
			removeTag,
			addAllTags,
			removeAllTags,
			addCacheComponent,
			removeCacheComponent,
			removeCacheComponentList,
			removeAllCache
		}
	},
	{
		persist: {
			key: 'appStore',
			storage: localStorage
		}
	}
)

/**
 * 查找路径对应的 meta.title
 */
const findRouteByPath = (targetPath: string): Breadcrumb => {
	// 使用 Vue Router 的路径匹配算法
	const matchedRoute = router.resolve(targetPath)

	if (matchedRoute?.meta?.title) {
		return {
			title: matchedRoute.meta.title as string,
			icon: matchedRoute.meta.icon as string
		}
	} else {
		return { title: '', icon: '' }
	}
}
