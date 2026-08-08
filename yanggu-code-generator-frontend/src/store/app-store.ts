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

	// 内容区最大化（CSS 方式，不影响 Fullscreen API，避免 teleport 弹出层被遮挡）
	const isFullscreen = ref(false)
	const toolFullscreen = () => {
		isFullscreen.value = !isFullscreen.value
	}

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
		isFullscreen,
		isDark,
		toggleCollapse,
		toolFullscreen,
		setLayoutSize,
		toggleDark
	}
})
