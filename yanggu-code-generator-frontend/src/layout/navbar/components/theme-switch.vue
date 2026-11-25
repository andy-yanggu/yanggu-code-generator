<template>
	<el-tooltip :content="appStore.isDark ? '切换到日间模式' : '切换到夜间模式'">
		<el-switch :model-value="appStore.isDark" inline-prompt :active-action-icon="Moon" :inactive-action-icon="Sunny" @click="handleThemeToggle"></el-switch>
	</el-tooltip>
</template>

<script setup lang="ts">
import { nextTick } from 'vue'
import { useAppStore } from '@/store'
import { Moon, Sunny } from '@element-plus/icons-vue'

defineOptions({
	name: 'ThemeSwitch'
})

const appStore = useAppStore()

/**
 * 正在进行中的 View Transition
 *
 * 连点时用它跳过上一次还没跑完的切换，
 * 避免两份快照叠在同一组伪元素上。
 *
 * 这里用结构类型而不是 ViewTransition，不依赖 lib.dom 是否已收录该类型。
 */
let currentTransition: { skipTransition(): void } | null = null

/**
 * 主题切换动画
 *
 * 几何量（圆心、半径）在这里算好写成 CSS 变量，
 * 动画本身交给文件末尾的 @keyframes theme-transition-circle 由样式引擎驱动。
 *
 * 两个方向互为逆过程，圆心始终是点击位置，只有半径在变：
 *
 * ☀️ light → 🌙 dark（收缩）：
 * 旧页面 light 盖在上层，以点击处为圆心从全屏圆收缩到 0，逐渐露出下层的新页面 dark
 *
 * 🌙 dark → ☀️ light（扩散）：
 * 新页面 light 盖在上层，以点击处为圆心从 0 扩散到全屏圆，逐渐遮盖下层的旧页面 dark
 */
const handleThemeToggle = (event: MouseEvent) => {
	/**
	 * 当前主题
	 *
	 * true  = dark
	 * false = light
	 */
	const isDarkNow = appStore.isDark

	/**
	 * ========================================
	 * 1. 浏览器不支持 View Transition
	 * ========================================
	 */
	if (!('startViewTransition' in document)) {
		appStore.toggleDark()
		return
	}

	/**
	 * ========================================
	 * 2. 动画几何量
	 * ========================================
	 *
	 * 圆心 = 点击位置。
	 * clientX / clientY 正好对应 clip-path 的 viewport 坐标系。
	 *
	 * 半径 = 点击点到最远角的距离：
	 * 横向取左右两侧较远的一段，纵向取上下两侧较远的一段，勾股即最远角距离。
	 * 以它为半径的圆必定覆盖整个视口，收缩到 0 时也不会漏掉任何角落。
	 *
	 * 两端圆心必须一致，只让半径变化。
	 * clip-path 在两个圆心不同的圆之间插值时，浏览器会同时线性插值圆心与半径，
	 * 实测（视口 1707×769，点击位置 1594,26，500ms）
	 * 视口仍被旧页面覆盖的比例：
	 *
	 * 圆心从左下角漂到点击位置
	 *   0ms 1.000 / 100ms 0.999 / 150ms 0.997 / 200ms 0.903 / 250ms 0.618 / 300ms 0.301
	 *   前 175ms 只变化 0.3%，七成变化挤进 150~300ms，
	 *   观感就是点了没反应然后突然刷一下
	 *
	 * 圆心固定在点击位置
	 *   0ms 1.000 / 100ms 0.782 / 150ms 0.620 / 200ms 0.460 / 250ms 0.274 / 300ms 0.142
	 *   变化从头到尾均匀铺开
	 */
	const x = event.clientX
	const y = event.clientY
	const radius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))

	/**
	 * ========================================
	 * 3. 创建 View Transition
	 * ========================================
	 *
	 * 连点时先跳过上一次还没跑完的切换。
	 */
	currentTransition?.skipTransition()

	const transition = document.startViewTransition(async () => {
		const root = document.documentElement

		/**
		 * 几何量与方向必须在更新回调内部写，不能在 startViewTransition 之前写：
		 *
		 * 回调之前写，浏览器要先为这次变量变更做一整轮样式重算，
		 * 紧接着捕获旧快照、再为类名变更重算一轮，白白多冻结一轮；
		 * 回调之内写，变量变更与主题类名变更合并成同一轮重算，
		 * 而且这一轮本来就是新快照捕获必须做的，不额外花时间。
		 *
		 * 伪元素树是在新快照捕获时才建立的，此时这些值早已就位，
		 * 动画照样能在 transition 的第一帧起跑。
		 */
		root.style.setProperty('--theme-transition-x', `${x}px`)
		root.style.setProperty('--theme-transition-y', `${y}px`)
		root.style.setProperty('--theme-transition-radius', `${radius}px`)

		/**
		 * 本次方向。
		 *
		 * 每次切换都按本次方向整体覆写，切换结束后不主动移除：
		 * 移除 :root 的属性会让整页样式重新计算（实测 3400 节点约 10ms），
		 * 正好落在动画收尾那一帧上，表现为动画刚放完又顿一下。
		 * 它只作用于 view-transition 伪元素，留着不影响页面渲染。
		 */
		root.setAttribute('data-theme-transition', isDarkNow ? 'to-light' : 'to-dark')

		/**
		 * 切换主题
		 */
		appStore.toggleDark()

		/**
		 * 等待 Vue 完成 DOM 更新
		 */
		await nextTick()
	})

	currentTransition = transition

	/**
	 * ========================================
	 * 4. View Transition 整体完成
	 * ========================================
	 *
	 * 动画由 CSS 声明，伪元素树连同 fill: both 的终态会随 transition 一起销毁，
	 * 这里没有动画需要清理，只把进行中的引用放下。
	 *
	 * 被新一轮切换接管时上一次的 finished 也会结算，
	 * 所以先比对是不是本次，避免抹掉刚建好的引用。
	 * 连点被 skipTransition 接管的切换以 AbortError 结算，属预期内，不上报。
	 */
	const cleanupThemeTransition = () => {
		if (currentTransition === transition) {
			currentTransition = null
		}
	}

	transition.finished.then(cleanupThemeTransition, error => {
		cleanupThemeTransition()

		if ((error as Error)?.name !== 'AbortError') {
			console.error('❌ ViewTransition finished error:', error)
		}
	})
}
</script>

<style>
/**
 * ============================================================
 * 默认层级（dark → light 扩散）
 * ============================================================
 *
 * 新页面位于上层，以点击处为圆心扩散到全屏，
 * 覆盖下层的旧页面。
 */
::view-transition-old(root) {
	z-index: 1;
}

::view-transition-new(root) {
	z-index: 2;
}

/**
 * ============================================================
 * 禁用浏览器默认的 View Transition 动画
 * ============================================================
 *
 * 默认的淡入淡出与缩放会让明暗两版快照叠在一起糊成一片，
 * 完全由 clip-path 控制主题切换效果。
 *
 * 这里不加 !important，下面按方向声明的动画要靠选择器权重覆盖它。
 */
::view-transition-group(root),
::view-transition-image-pair(root),
::view-transition-old(root),
::view-transition-new(root) {
	animation: none;
	mix-blend-mode: normal;
}

/**
 * ============================================================
 * 圆形揭示关键帧
 * ============================================================
 *
 * 只写一份：从全屏圆收缩到 0。
 * 收缩方向直接播，扩散方向用 animation-direction: reverse 倒播，
 * 倒播同时把缓动曲线也镜像，两个方向的观感天然对称。
 *
 * 三个几何量由 script 在更新回调里写到 :root 上，
 * 兜底值只在样式还没写过它们时生效（正常路径不会用到）。
 */
@keyframes theme-transition-circle {
	from {
		clip-path: circle(var(--theme-transition-radius, 150%) at var(--theme-transition-x, 50%) var(--theme-transition-y, 50%));
	}

	to {
		clip-path: circle(0px at var(--theme-transition-x, 50%) var(--theme-transition-y, 50%));
	}
}

/**
 * ============================================================
 * 收缩模式（light → dark）
 * ============================================================
 *
 * 旧页面 light 换到上层，半径收到 0，露出下层的新页面。
 */
:root[data-theme-transition='to-dark']::view-transition-old(root) {
	z-index: 2;
	animation: 0.4s linear both theme-transition-circle;
}

:root[data-theme-transition='to-dark']::view-transition-new(root) {
	z-index: 1;
}

/**
 * ============================================================
 * 扩散模式（dark → light）
 * ============================================================
 *
 * 新页面 light 留在上层倒播同一份关键帧，半径从 0 长到全屏。
 */
:root[data-theme-transition='to-light']::view-transition-new(root) {
	z-index: 2;
	animation: 0.4s linear 1 reverse both theme-transition-circle;
}

:root[data-theme-transition='to-light']::view-transition-old(root) {
	z-index: 1;
}

/**
 * ============================================================
 * 减少动画
 * ============================================================
 *
 * 动画改成 CSS 声明之后，这条才真正起作用：
 * 系统偏好减少动效时直接瞬时切换，不再播放圆形揭示。
 */
@media (prefers-reduced-motion: reduce) {
	:root[data-theme-transition]::view-transition-old(root),
	:root[data-theme-transition]::view-transition-new(root) {
		animation: none !important;
	}
}
</style>
