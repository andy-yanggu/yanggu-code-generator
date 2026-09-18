/**
 * 布局 DOM 引用 composable
 *
 * 管理布局相关的 DOM 引用和全屏状态，替代原先存储在 Pinia Store 中的 DOM ref。
 * 采用模块级单例模式：ref 在模块作用域创建一次，所有调用方共享同一实例。
 *
 * 使用方式：
 *   const { layoutMainRef, layoutScrollbarRef, currentFullscreenElement } = useLayout()
 */

// 模块级单例：ref 只创建一次，跨组件共享
const layoutMainRef = ref<HTMLElement>()
const layoutScrollbarRef = ref<any>()
const currentFullscreenElement = ref<HTMLElement | null>(null)

// 全屏状态监听：只注册一次
useEventListener(document, 'fullscreenchange', () => {
	currentFullscreenElement.value = document.fullscreenElement as HTMLElement | null
})

export const useLayout = () => {
	return {
		layoutMainRef,
		layoutScrollbarRef,
		currentFullscreenElement
	}
}
