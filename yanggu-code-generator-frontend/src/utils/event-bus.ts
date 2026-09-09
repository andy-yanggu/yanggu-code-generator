import { useEventBus } from '@vueuse/core'
import type { EventBusKey } from '@vueuse/core'

// 字段级偏好变更事件
export interface PreferenceFieldEvent<T = undefined> {
	path: string
	before: T
	after: T
}

// 4 个字段级事件总线
const titleChangeBusKey: EventBusKey<PreferenceFieldEvent<string | undefined>> = Symbol('titleChange')
const cacheChangeBusKey: EventBusKey<PreferenceFieldEvent<boolean | undefined>> = Symbol('cacheChange')
const hideMenuChangeBusKey: EventBusKey<PreferenceFieldEvent<boolean | undefined>> = Symbol('hideMenuChange')
const hideTabChangeBusKey: EventBusKey<PreferenceFieldEvent<boolean | undefined>> = Symbol('hideTabChange')

export const titleChangeBus = useEventBus(titleChangeBusKey)
export const cacheChangeBus = useEventBus(cacheChangeBusKey)
export const hideMenuChangeBus = useEventBus(hideMenuChangeBusKey)
export const hideTabChangeBus = useEventBus(hideTabChangeBusKey)
