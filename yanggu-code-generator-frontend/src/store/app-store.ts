import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useSystemSettingStore } from '@/store/system-setting-store'
import { useDark, useToggle } from '@vueuse/core'

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

export interface IframeInfo {
	// 路由名称
	name: string
	// 路由地址
	src: string
	// 全路径
	fullPath: string
}

export type LayOutSize = 'large' | 'default' | 'small'

export const useAppStore = defineStore(
	'app',
	() => {
		// 状态
		// 折叠状态
		const isCollapse = ref(false)
		// 标签列表
		const tagList = ref<NavbarTag[]>([])
		// 缓存组件列表
		const cacheList = ref<string[]>([])
		// 布局大小
		const layoutSize = ref<LayOutSize>('default')
		// iframe缓存列表
		const iframeCacheList = ref<IframeInfo[]>([])
		// 主题模式
		const isDark = useDark({
			selector: 'html',
			attribute: 'class',
			valueDark: 'dark',
			valueLight: 'light'
		})

		// 计算属性
		// 标签数量
		const tagLength = computed(() => tagList.value.length)

		// actions
		// 切换折叠状态
		const toggleCollapse = () => {
			isCollapse.value = !isCollapse.value
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

		// 批量删除标签
		const removeTags = (removeTagList: NavbarTag[]) => {
			tagList.value = tagList.value.filter(item => !removeTagList.some(tag => tag.fullPath === item.fullPath))
		}

		// 添加所有标签
		const addAllTags = (addTagList: NavbarTag[]) => {
			tagList.value = addTagList
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

		// 删除全部iframe路由
		const removeAllIframeCache = () => {
			iframeCacheList.value = []
		}

		// 设置布局大小
		const setLayoutSize = (size: LayOutSize) => {
			layoutSize.value = size
		}

		// 切换主题模式
		const toggleDark = useToggle(isDark)

		return {
			isCollapse,
			tagList,
			layoutSize,
			cacheList,
			iframeCacheList,
			isDark,
			tagLength,
			toggleCollapse,
			addTag,
			removeTag,
			addAllTags,
			removeAllTags,
			removeTags,
			addCacheComponent,
			removeCacheComponent,
			removeCacheComponentList,
			removeAllCache,
			addIframeCache,
			removeIframeCache,
			removeIframeCacheList,
			removeAllIframeCache,
			setLayoutSize,
			toggleDark
		}
	},
	{
		// @ts-expect-error - 忽略下一行的类型检查
		persist: {
			key: 'appStore',
			storage: localStorage,
			// @ts-expect-error - 忽略下一行的类型检查
			// 支持动态配置忽略字段
			omit: _ => {
				const originOmitList = []
				const systemSettingStore = useSystemSettingStore()
				if (!systemSettingStore.isOpenTagCache) {
					originOmitList.push('tagList')
				}
				return originOmitList
			}
		}
	}
)
