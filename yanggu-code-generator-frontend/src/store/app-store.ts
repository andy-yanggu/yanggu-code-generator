import { type LayoutSize } from '@/constant/enum'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'

// 持久化配置
const getPersistConfig = () => {
	// 始终忽略的字段（加载状态和响应式主题，不应持久化）
	const alwaysOmitList = ['globalLoading', 'isDark', 'isFullscreen']

	return {
		key: 'appStore',
		storage: localStorage,
		// omit 保持静态：仅包含始终需要忽略的字段
		omit: alwaysOmitList
	} as PersistenceOptions
}

export const useAppStore = defineStore(
	'app',
	() => {
		// 折叠状态
		const isCollapse = ref(false)
		// 全局 loading
		const globalLoading = ref(false)
		// 布局大小
		const layoutSize = ref<LayoutSize>('default')
		// 主题模式
		// disableTransition: false 关闭 vueuse 的「临时注入 *{transition:none} + 强制样式重算」保护。
		// 实测该保护在 3400+ 节点页面上要多花两次全量样式重算（注入约 20ms + 移除约 20ms），
		// 且这两次重算全部落在主题切换动画的「点击 → 动画开始」冻结窗口内。
		// 关闭后类名变更引发的那一次重算（约 25ms）本来就必须做，属于切换主题的固有成本。
		// 代价是切换过程中 el-menu 等带 color 过渡的元素在新快照里颜色略有滞后（约 12%），
		// 而 View Transition 期间实时页面被快照遮挡，过渡在动画结束前早已完成，肉眼不可见。
		const isDark = useDark({
			selector: 'html',
			attribute: 'class',
			valueDark: 'dark',
			valueLight: 'light',
			disableTransition: false
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
		const setLayoutSize = (size: LayoutSize) => {
			layoutSize.value = size
		}

		// 切换主题模式
		const toggleDark = useToggle(isDark)

		return {
			isCollapse,
			globalLoading,
			layoutSize,
			isFullscreen,
			isDark,
			toggleCollapse,
			toolFullscreen,
			setLayoutSize,
			toggleDark
		}
	},
	{
		persist: getPersistConfig()
	}
)
