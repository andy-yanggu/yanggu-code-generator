import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { MenuPreferenceItem, MenuPreferenceMap } from '@/types'
import { titleChangeBus, cacheChangeBus, hideMenuChangeBus, hideTabChangeBus } from '@/utils/event-bus'

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

		// 字段级变更事件发布：对比 before/after 的每个字段，仅 emit 实际变化的字段
		const emitFieldChanges = (path: string, before: MenuPreferenceItem, after: MenuPreferenceItem | undefined) => {
			const fields: (keyof MenuPreferenceItem)[] = ['title', 'cache', 'hideMenu', 'hideTab']
			for (const field of fields) {
				const beforeVal = before[field]
				const afterVal = after?.[field]
				if (beforeVal !== afterVal) {
					const event = { path, before: beforeVal, after: afterVal }
					switch (field) {
						case 'title':
							titleChangeBus.emit(event as any)
							break
						case 'cache':
							cacheChangeBus.emit(event as any)
							break
						case 'hideMenu':
							hideMenuChangeBus.emit(event as any)
							break
						case 'hideTab':
							hideTabChangeBus.emit(event as any)
							break
					}
				}
			}
		}

		// 同步偏好（合并后清洗掉与服务端默认值相同的字段，确保 preferenceMap 只保留真正偏离默认的项）
		const syncPreference = (path: string, item: MenuPreferenceItem, defaults: MenuPreferenceItem) => {
			const before = { ...(preferenceMap.value[path] || {}) }
			const existing = preferenceMap.value[path] || {}
			const merged = { ...existing, ...item }
			// 清洗：等于服务端默认的字段不存储
			for (const key of Object.keys(merged) as (keyof MenuPreferenceItem)[]) {
				if (merged[key] === defaults[key]) {
					delete merged[key]
				}
			}
			const hasAnyValue = Object.values(merged).some(v => v !== undefined)
			if (hasAnyValue) {
				preferenceMap.value[path] = merged
			} else {
				delete preferenceMap.value[path]
			}
			emitFieldChanges(path, before, preferenceMap.value[path])
		}

		// 移除指定路径的偏好
		const removePreference = (path: string) => {
			const before = { ...(preferenceMap.value[path] || {}) }
			delete preferenceMap.value[path]
			emitFieldChanges(path, before, undefined)
		}

		// 重置所有偏好
		const resetAll = () => {
			const snapshot = { ...preferenceMap.value }
			preferenceMap.value = {}
			for (const path of Object.keys(snapshot)) {
				emitFieldChanges(path, snapshot[path], undefined)
			}
		}

		// 统一获取指定路径的所有有效值（用户偏好 > 服务端默认）
		// 一次调用拿到全部 4 个字段的最终值，避免消费方多次调用
		const getEffective = (
			path: string,
			serverDefaults: { title?: string; cache?: boolean; hideMenu?: boolean; hideTab?: boolean }
		) => {
			const pref = preferenceMap.value[path]
			return {
				title: pref?.title || serverDefaults.title || '',
				cache: pref?.cache ?? serverDefaults.cache ?? false,
				hideMenu: pref?.hideMenu ?? serverDefaults.hideMenu ?? false,
				hideTab: pref?.hideTab ?? serverDefaults.hideTab ?? false
			}
		}

		return {
			preferenceMap,
			getPreference,
			syncPreference,
			removePreference,
			resetAll,
			getEffective
		}
	},
	{
		persist: getPersistConfig()
	}
)
