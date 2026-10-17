import { findMenuByPath } from '@/utils/menu'
import { ROUTE_META_DEFAULTS } from '@/config/router'
import { useMenuPreferenceStore, useUserStore } from '@/store'
import type { MenuInfo } from '@/types'

/**
 * 菜单偏好 composable
 *
 * 统一封装「用户偏好 > 服务端默认」的有效值计算，返回响应式 computed。
 * 由于 store 的 preferenceMap 与 userStore 的 menuList 都是响应式的，
 * 偏好或菜单变化时计算属性会自动重算，消费方无需再监听字段变更事件。
 *
 * 服务端默认值默认按 path 从 userStore.menuList 反查（findMenuByPath），
 * 缺省字段回退到 ROUTE_META_DEFAULTS；也可通过第二个参数显式覆盖。
 *
 * 使用方式：
 *   const { effective, title, cache, hideMenu, hideTab } = useMenuPreference(() => props.menu.path)
 *   const { cache } = useCurrentMenuPreference()
 */

// 服务端默认值形状（与 store.getEffective 的 serverDefaults 参数一致）
export interface MenuServerDefaults {
	title?: string
	cache?: boolean
	hideMenu?: boolean
	hideTab?: boolean
}

// 按绝对路径从菜单树解析服务端默认值，缺省项回退到 ROUTE_META_DEFAULTS
const resolveServerDefaults = (menuList: MenuInfo[], path: string): MenuServerDefaults => {
	const meta = findMenuByPath(menuList, path)?.meta
	return {
		title: meta?.title ?? '',
		cache: meta?.cache ?? ROUTE_META_DEFAULTS.cache,
		hideMenu: meta?.hideMenu ?? ROUTE_META_DEFAULTS.hideMenu,
		hideTab: meta?.hideTab ?? ROUTE_META_DEFAULTS.hideTab
	}
}

/**
 * 计算指定路径菜单的有效偏好值
 *
 * @param path 菜单绝对路径，支持静态值 / ref / getter
 * @param serverDefaults 可选，显式指定服务端默认值；不传则按 path 从 userStore 解析
 */
export const useMenuPreference = (path: MaybeRefOrGetter<string>, serverDefaults?: MaybeRefOrGetter<MenuServerDefaults>) => {
	const menuPreferenceStore = useMenuPreferenceStore()
	const userStore = useUserStore()

	// 单一基底 computed：一次拿到全部 4 个字段的有效值
	const effective = computed(() => {
		const targetPath = toValue(path)
		const defaults = serverDefaults !== undefined ? toValue(serverDefaults) : resolveServerDefaults(userStore.menuList, targetPath)
		return menuPreferenceStore.getEffective(targetPath, defaults)
	})

	return {
		// 整体对象（模板中 effective.title 自动解包，脚本中 effective.value.title）
		effective,
		// 分字段 computed（从基底派生，避免重复计算）
		title: computed(() => effective.value.title),
		cache: computed(() => effective.value.cache),
		hideMenu: computed(() => effective.value.hideMenu),
		hideTab: computed(() => effective.value.hideTab)
	}
}

/**
 * useMenuPreference 的便捷变体：绑定当前路由
 *
 * 服务端默认值按当前 route.path 从 userStore 解析，无需手动传入 route.meta。
 */
export const useCurrentMenuPreference = () => {
	const route = useRoute()
	return useMenuPreference(() => route.path)
}
