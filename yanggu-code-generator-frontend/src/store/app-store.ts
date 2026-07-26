import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useDark, useEventListener, useFullscreen, useToggle } from '@vueuse/core'
import { LayOutSize } from '@/types'

export const useAppStore = defineStore('app', () => {
	// 折叠状态
	const isCollapse = ref(false)
	// 全局 loading
	const globalLoading = ref(false)
	// 布局大小
	const layoutSize = ref<LayOutSize>('default')
	// Layout Main Ref
	const layoutMainRef = ref()
	// Layout Main 滚动条 ref
	const layoutScrollbarRef = ref()
	// 全屏元素
	const currentFullscreenElement = ref<HTMLElement | null>(null)
	// 主题模式
	const isDark = useDark({
		selector: 'html',
		attribute: 'class',
		valueDark: 'dark',
		valueLight: 'light'
	})

	useEventListener(document, 'fullscreenchange', () => {
		currentFullscreenElement.value = document.fullscreenElement as HTMLElement | null
	})

	// 切换折叠状态
	const toggleCollapse = () => {
		isCollapse.value = !isCollapse.value
	}

	// 全屏切换
	const { toggle: toolFullscreen } = useFullscreen(layoutMainRef)

	// 设置布局大小
	const setLayoutSize = (size: LayOutSize) => {
		layoutSize.value = size
	}

	// 切换主题模式
	const toggleDark = useToggle(isDark)

	return {
		isCollapse,
		globalLoading,
		layoutSize,
		layoutMainRef,
		layoutScrollbarRef,
		currentFullscreenElement,
		isDark,
		toggleCollapse,
		toolFullscreen,
		setLayoutSize,
		toggleDark
	}
})
// appStore 不需要持久化（DOM 引用和 loading 不应持久化）
