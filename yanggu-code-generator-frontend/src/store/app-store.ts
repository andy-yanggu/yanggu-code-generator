import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useSystemSettingStore } from '@/store'
import { useDark, useEventListener, useFullscreen, useToggle } from '@vueuse/core'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { IframeInfo, LayOutSize, NavbarTag } from '@/types'

// 持久化配置
const getPersistConfig = () => {
	const key = 'appStore'
	// 根据环境设置持久化配置
	// if (import.meta.env.PROD) {
	//
	// }
	// 始终忽略的字段（DOM 引用和加载状态，不应持久化）
	const alwaysOmitList = ['layoutMainRef', 'currentFullscreenElement', 'layoutScrollbarRef', 'globalLoading']
	// 标签相关字段
	const tagOmitList = ['tagList', 'activeTabPath']

	return {
		key,
		storage: localStorage,
		// omit 保持静态：仅包含始终需要忽略的字段
		omit: alwaysOmitList,
		// 利用 serializer 在每次保存时动态过滤
		// 这是实现「动态字段过滤」的正确姿势，每次保存都会重新评估
		serializer: {
			serialize: (state: Record<string, any>) => {
				const filteredState = { ...state }
				const systemSettingStore = useSystemSettingStore()
				// 如果关闭标签缓存，运行时移除标签相关字段
				if (!systemSettingStore.tag.isOpenTagCache) {
					tagOmitList.forEach(field => delete filteredState[field])
				}
				return JSON.stringify(filteredState)
			},
			deserialize: JSON.parse
		}
	} as PersistenceOptions
}

export const useAppStore = defineStore(
	'app',
	() => {
		// 状态
		// 折叠状态
		const isCollapse = ref(false)
		// 全局loading
		const globalLoading = ref(false)
		// 激活tab
		const activeTabPath = ref('')
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
		// Layout Main Ref
		const layoutMainRef = ref()
		// Layout Main滚动条ref
		const layoutScrollbarRef = ref()

		// 全屏元素
		const currentFullscreenElement = ref<HTMLElement | null>(null)

		useEventListener(document, 'fullscreenchange', () => {
			currentFullscreenElement.value = document.fullscreenElement as HTMLElement | null
		})

		// 计算属性
		// 标签数量
		const tagLength = computed(() => tagList.value.length)

		// actions
		// 切换折叠状态
		const toggleCollapse = () => {
			isCollapse.value = !isCollapse.value
		}

		// 全屏展开
		const { toggle: toolFullscreen } = useFullscreen(layoutMainRef)

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

		// 添加固定标签相关方法
		const togglePinTag = (tag: NavbarTag) => {
			const targetTag = tagList.value.find(t => t.fullPath === tag.fullPath)
			if (!targetTag) {
				return
			}

			if (targetTag.pinned) {
				targetTag.pinned = false
				targetTag.pinnedAt = null
			} else {
				targetTag.pinned = true
				targetTag.pinnedAt = Date.now()
			}

			// 排序固定标签置顶
			addAllTags(sortTags(tagList.value))
		}

		// 标签排序：固定标签置顶 + 固定标签按 pinnedAt 顺序
		const sortTags = (tags: NavbarTag[]) => {
			const pinnedTags = tags.filter(t => t.pinned).sort((a, b) => a.pinnedAt! - b.pinnedAt!)
			const normalTags = tags.filter(t => !t.pinned)
			return [...pinnedTags, ...normalTags]
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
			globalLoading,
			activeTabPath,
			tagList,
			layoutSize,
			cacheList,
			iframeCacheList,
			isDark,
			layoutMainRef,
			layoutScrollbarRef,
			currentFullscreenElement,
			tagLength,
			toggleCollapse,
			toolFullscreen,
			addTag,
			removeTag,
			addAllTags,
			removeAllTags,
			removeTags,
			togglePinTag,
			sortTags,
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
		persist: getPersistConfig()
	}
)
