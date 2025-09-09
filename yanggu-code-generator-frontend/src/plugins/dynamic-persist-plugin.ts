import type { PiniaPluginContext } from 'pinia'

/**
 * 支持 pick/omit 可以写函数的增强版 persist 插件
 */
export const dynamicPersistPlugin = ({ store, options }: PiniaPluginContext) => {
	const persist = (options as any)?.persist
	if (!persist) {
		return
	}

	const storage = persist.storage || localStorage
	const key = persist.key || store.$id

	// 解析 state 并根据 pick/omit 过滤
	const resolvePaths = (state: any) => {
		let pick: string[] | undefined
		let omit: string[] | undefined

		if (typeof persist.pick === 'function') {
			pick = persist.pick(store) // 运行时动态计算
		} else {
			pick = persist.pick
		}

		if (typeof persist.omit === 'function') {
			omit = persist.omit(store)
		} else {
			omit = persist.omit
		}

		const newState: Record<string, any> = { ...state }

		if (pick && pick.length > 0) {
			return pick.reduce(
				(acc, key) => {
					acc[key] = state[key]
					return acc
				},
				{} as Record<string, any>
			)
		}

		if (omit && omit.length > 0) {
			omit.forEach(key => delete newState[key])
			return newState
		}

		return newState
	}

	// 初始化恢复
	const fromStorage = storage.getItem(key)
	if (fromStorage) {
		try {
			store.$patch(JSON.parse(fromStorage))
		} catch (err) {
			console.error(`[dynamicPersistPlugin] Failed to parse storage for ${key}`, err)
		}
	}

	// 订阅变化时保存
	store.$subscribe((_mutation, state) => {
		const filtered = resolvePaths(state)
		storage.setItem(key, JSON.stringify(filtered))
	})
}
