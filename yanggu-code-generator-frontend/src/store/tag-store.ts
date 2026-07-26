import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { NavbarTag } from '@/types'
import { useSystemSettingStore } from '@/store'

// 持久化配置
const getPersistConfig = () => {
	return {
		key: 'tagStore',
		storage: localStorage,
		// 利用 serializer 在每次保存时动态过滤
		serializer: {
			serialize: (state: Record<string, any>) => {
				const systemSettingStore = useSystemSettingStore()
				// 如果关闭标签缓存，运行时移除标签相关字段
				if (!systemSettingStore.tag.isOpenTagCache) {
					return '{}'
				}
				return JSON.stringify(state)
			},
			deserialize: JSON.parse
		}
	} as PersistenceOptions
}

export const useTagStore = defineStore(
	'tag',
	() => {
		// 激活 tab 路径
		const activeTabPath = ref('')
		// 标签列表
		const tagList = ref<NavbarTag[]>([])

		// 计算属性
		const tagLength = computed(() => tagList.value.length)

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

		// 切换标签固定状态
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

		return {
			activeTabPath,
			tagList,
			tagLength,
			addTag,
			removeTag,
			removeTags,
			addAllTags,
			removeAllTags,
			togglePinTag,
			sortTags
		}
	},
	{
		persist: getPersistConfig()
	}
)
