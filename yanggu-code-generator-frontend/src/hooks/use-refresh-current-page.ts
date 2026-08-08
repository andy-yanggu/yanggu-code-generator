import { useCacheStore, useSystemSettingStore } from '@/store'

// 刷新当前页面
export const usePageRefresher = () => {
	const cacheStore = useCacheStore()
	const systemSettingStore = useSystemSettingStore()
	const route = useRoute()
	const router = useRouter()

	const refreshPage = () => {
		const { type, cache } = route.meta
		const name = route.name as string

		// 关闭了全局页面缓存，使用路由跳转实现刷新
		if (!systemSettingStore.other.isOpenPageCache) {
			// 使用路由跳转实现刷新
			router.push({
				path: '/redirect' + route.fullPath,
				query: route.query
			})
		} else {
			// 开启了全局页面缓存，使用组件缓存实现刷新
			// 业务菜单
			if (type === 1) {
				// 如果是缓存，删除缓存
				if (cache) {
					cacheStore.removeCacheComponent(name)
				}

				// 使用路由跳转实现刷新
				router.push({
					path: '/redirect' + route.fullPath,
					query: route.query
				})
			} else if (type === 3 && cache === false) {
				// iframe菜单，非缓存
				// 使用路由跳转实现刷新
				router.push({
					path: '/redirect' + route.fullPath,
					query: route.query
				})
			} else {
				// iframe菜单，缓存
				const idx = cacheStore.iframeCacheList.findIndex(i => i.fullPath === route.fullPath)
				if (idx !== -1) {
					const old = cacheStore.iframeCacheList[idx]
					// 修改src的值，会触发刷新
					old.src = `${old.src.split('?')[0]}?t=${Date.now()}`
				}
			}
		}
	}
	return { refreshPage }
}
