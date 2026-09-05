import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { MenuPreferenceItem, MenuPreferenceMap } from '@/types'

// 持久化配置
const getPersistConfig = () => {
	return {
		key: 'menuPreferenceStore',
		storage: localStorage
	} as PersistenceOptions
}

export const useMenuPreferenceStore = defineStore(
	'menu-preference',
	() => {
		// 偏好映射表：只存储用户修改过的项
		const preferenceMap = ref<MenuPreferenceMap>({})

		// 获取指定路径的偏好
		const getPreference = (path: string): MenuPreferenceItem | undefined => {
			return preferenceMap.value[path]
		}

		// 设置指定路径的偏好（合并更新）
		const setPreference = (path: string, item: MenuPreferenceItem) => {
			const existing = preferenceMap.value[path] || {}
			preferenceMap.value[path] = { ...existing, ...item }
		}

		// 移除指定路径的偏好
		const removePreference = (path: string) => {
			delete preferenceMap.value[path]
		}

		// 重置所有偏好
		const resetAll = () => {
			preferenceMap.value = {}
		}

		// 便捷方法：获取有效值（用户偏好 > 服务端默认）
		const getEffectiveCache = (path: string, serverDefault: boolean): boolean => {
			return preferenceMap.value[path]?.cache ?? serverDefault
		}

		const getEffectiveHideMenu = (path: string, serverDefault: boolean): boolean => {
			return preferenceMap.value[path]?.hideMenu ?? serverDefault
		}

		const getEffectiveHideTab = (path: string, serverDefault: boolean): boolean => {
			return preferenceMap.value[path]?.hideTab ?? serverDefault
		}

		return {
			preferenceMap,
			getPreference,
			setPreference,
			removePreference,
			resetAll,
			getEffectiveCache,
			getEffectiveHideMenu,
			getEffectiveHideTab
		}
	},
	{
		persist: getPersistConfig()
	}
)
