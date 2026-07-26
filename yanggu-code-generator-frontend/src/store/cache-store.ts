import { defineStore } from 'pinia'
import { ref } from 'vue'
import { isEmpty } from '@/utils/tool'
import { IframeInfo } from '@/types'

export const useCacheStore = defineStore('cache', () => {
	// 组件缓存列表
	const cacheList = ref<string[]>([])

	// 添加缓存组件
	const addCacheComponent = (name: string) => {
		if (!cacheList.value.includes(name)) {
			cacheList.value.push(name)
		}
	}

	// 删除缓存组件
	const removeCacheComponent = (name: string) => {
		const indexOf = cacheList.value.indexOf(name)
		if (indexOf > -1) {
			cacheList.value.splice(indexOf, 1)
		}
	}

	// 批量删除缓存组件
	const removeCacheComponentList = (nameList: string[]) => {
		if (isEmpty(nameList)) {
			return
		}
		const removeSet = new Set(nameList)
		cacheList.value = cacheList.value.filter(name => !removeSet.has(name))
	}

	// 删除所有缓存
	const removeAllCache = () => {
		cacheList.value = []
	}

	// iframe 缓存列表
	const iframeCacheList = ref<IframeInfo[]>([])

	// 添加 iframe 缓存
	const addIframeCache = (iframeInfo: IframeInfo) => {
		if (iframeCacheList.value.some(item => item.fullPath === iframeInfo.fullPath)) {
			return
		}
		iframeCacheList.value.push(iframeInfo)
	}

	// 删除 iframe 缓存
	const removeIframeCache = (name: string) => {
		iframeCacheList.value = iframeCacheList.value.filter(item => item.name !== name)
	}

	// 批量删除 iframe 缓存
	const removeIframeCacheList = (nameList: string[]) => {
		if (isEmpty(nameList)) {
			return
		}
		const removeSet = new Set(nameList)
		iframeCacheList.value = iframeCacheList.value.filter(item => !removeSet.has(item.name))
	}

	// 删除所有 iframe 缓存
	const removeAllIframeCache = () => {
		iframeCacheList.value = []
	}

	return {
		cacheList,
		addCacheComponent,
		removeCacheComponent,
		removeCacheComponentList,
		removeAllCache,
		iframeCacheList,
		addIframeCache,
		removeIframeCache,
		removeIframeCacheList,
		removeAllIframeCache
	}
})
// 注意：cacheStore 不需要持久化，它是运行时状态
