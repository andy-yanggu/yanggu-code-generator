import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
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

export interface IframeInfo {
	// 路由名称
	name: string
	// 路由地址
	src: string
	// 全路径
	fullPath: string
}

export const useAppStore = defineStore(
	'app',
	() => {
		// 状态
		// 折叠状态
		const isCollapse = ref(false)
		// 标签列表
		const tagsList = ref<NavbarTag[]>([])
		// 面包屑列表
		const breadcrumbList = ref<Breadcrumb[]>([])
		// 缓存组件列表
		const cacheList = ref<string[]>([])
		// 布局大小
		const layoutSize = ref<'large' | 'default' | 'small'>('default')
		// iframe缓存列表
		const iframeCacheList = ref<IframeInfo[]>([])

		// 计算属性
		// 标签数量
		const tagLength = computed(() => tagsList.value.length)

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
				currentPath += `/${path}`
				const breadcrumb = findRouteByPath(currentPath)
				if (breadcrumb && breadcrumb.title && breadcrumb.icon) {
					matched.push(breadcrumb)
				}
			}
			breadcrumbList.value = matched
		}

		// 添加标签
		const addTag = (tag: NavbarTag) => {
			const isExist = tagsList.value.find(item => item.fullPath === tag.fullPath)
			const includes = tag.fullPath.includes('redirect')
			if (!isExist && !includes) {
				tagsList.value.push(tag)
			}
		}

		// 删除标签
		const removeTag = (tag: NavbarTag) => {
			tagsList.value = tagsList.value.filter(item => item.fullPath !== tag.fullPath)
		}

		// 批量删除标签
		const removeTagList = (tagList: NavbarTag[]) => {
			tagsList.value = tagsList.value.filter(item => !tagList.some(tag => tag.fullPath === item.fullPath))
		}

		// 添加所有标签
		const addAllTags = (tagList: NavbarTag[]) => {
			tagsList.value = tagList
		}

		// 删除所有标签
		const removeAllTags = () => {
			tagsList.value = []
		}

		// 添加缓存路由
		const addCacheComponent = (name: string) => {
			if (!cacheList.value.includes(name)) {
				cacheList.value.push(name)
			}
		}

		// 删除缓存路由
		const removeCacheComponent = (name: string) => {
			const indexOf = cacheList.value.indexOf(name)
			if (indexOf > -1) {
				cacheList.value.splice(indexOf, 1)
			}
		}

		// 批量删除缓存路由
		const removeCacheComponentList = (nameList: string[]) => {
			if (!nameList || nameList.length === 0) {
				return
			}
			for (const name of nameList) {
				removeCacheComponent(name)
			}
		}

		// 删除所有缓存
		const removeAllCache = () => {
			cacheList.value = []
		}

		// 添加iframe路由
		const addIframeCache = (iframeInfo: IframeInfo) => {
			if (iframeCacheList.value.some(item => item.fullPath === iframeInfo.fullPath)) {
				return
			}
			iframeCacheList.value.push(iframeInfo)
		}

		// 删除iframe路由
		const removeIframeCache = (name: string) => {
			iframeCacheList.value = iframeCacheList.value.filter(item => item.name !== name)
		}

		// 批量删除iframe路由
		const removeIframeCacheList = (nameList: string[]) => {
			if (!nameList || nameList.length === 0) {
				return
			}
			for (const name of nameList) {
				removeIframeCache(name)
			}
		}

		// 设置布局大小
		const setLayoutSize = (size: 'large' | 'default' | 'small') => {
			layoutSize.value = size
		}

		return {
			isCollapse,
			breadcrumbList,
			tagsList,
			layoutSize,
			cacheList,
			iframeCacheList,
			tagLength,
			toggleCollapse,
			setBreadcrumb,
			addTag,
			removeTag,
			addAllTags,
			removeAllTags,
			removeTagList,
			addCacheComponent,
			removeCacheComponent,
			removeCacheComponentList,
			removeAllCache,
			addIframeCache,
			removeIframeCache,
			removeIframeCacheList,
			setLayoutSize
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
