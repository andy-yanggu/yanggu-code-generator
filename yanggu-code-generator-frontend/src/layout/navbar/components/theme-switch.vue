<template>
	<icon-button
		:el-icon="appStore.isDark ? Moon : Sunny"
		:tooltip="appStore.isDark ? '切换到日间模式' : '切换到夜间模式'"
		size="18px"
		@click="handleThemeToggle"></icon-button>
</template>

<script setup lang="ts">
import { nextTick } from 'vue'
import { useAppStore } from '@/store'
import IconButton from '@/components/icon-button/index.vue'
import { Moon, Sunny } from '@element-plus/icons-vue'

defineOptions({
	name: 'ThemeSwitch'
})

const appStore = useAppStore()

/**
 * 取消遗留的 View Transition 动画
 *
 * fill: 'forwards' 的动画在 transition 结束后仍挂在 documentElement 上，
 * 并把终态 clip-path 继续作用到下一次切换的同名伪元素上。
 *
 * 实测后果：dark → light 全程
 * ::view-transition-old(root) = circle(0px at 上次点击位置)，
 * 旧页面快照整段不可见，圆扩散时底下是空白背景，
 * 观感上就是开头闪一下、动画不连贯。
 *
 * 正常结束时由 finished 回调取消，
 * 切换被跳过或异常导致回调没跑到时，
 * 在下次切换开始前再兜底清理一次。
 */
const cancelViewTransitionAnimations = () => {
	document
		.getAnimations()
		.filter(animation => (animation.effect as KeyframeEffect | null)?.pseudoElement?.includes('view-transition'))
		.forEach(animation => animation.cancel())
}

/**
 * 两个方向统一使用同一个时长
 *
 * 收缩与扩散互为逆过程，时长不一致会让来回手感不一样，
 * 扩散方向多出来的 300ms 也只是把卡顿感拉长。
 */
const transitionDuration = 500

/**
 * 正在进行中的 View Transition
 *
 * 连点时用它找到上一次切换并跳过，
 * 避免多份动画并发叠在同一组伪元素上。
 *
 * 这里用结构类型而不是 ViewTransition，不依赖 lib.dom 是否已收录该类型。
 */
let currentTransition: { skipTransition(): void } | null = null

/**
 * 监听主题切换动画的失败
 *
 * 连点时上一次动画会随上一次 transition 被取消，
 * 此时 finished 抛出的 AbortError 属于预期内，不再上报。
 */
const watchThemeAnimation = (animation: Animation) => {
	animation.finished.catch(error => {
		if ((error as Error)?.name !== 'AbortError') {
			console.error('❌ Theme animation error:', error)
		}
	})
}

/**
 * 主题切换动画
 *
 * light → dark（收缩）：
 * 旧页面 light 从左下角大圆收缩到点击位置，
 * 逐渐露出下层的新页面 dark
 *
 * dark → light（扩散）：
 * 新页面 light 从点击位置扩散到左下角大圆，
 * 逐渐覆盖下层的旧页面 dark
 *
 * 两个方向的动画互为逆过程，
 * 左下角是固定锚点，点击位置是动态端点。
 */
const handleThemeToggle = (event: MouseEvent) => {
	/**
	 * ========================================
	 * 1. 鼠标点击位置
	 * ========================================
	 *
	 * 使用 viewport 坐标。
	 *
	 * clientX / clientY 正好对应
	 * clip-path 的 viewport 坐标系。
	 */
	const x = event.clientX
	const y = event.clientY

	/**
	 * 当前主题
	 *
	 * true  = dark
	 * false = light
	 */
	const isDarkNow = appStore.isDark

	/**
	 * ========================================
	 * 2. 左下角锚点
	 * ========================================
	 *
	 * 收缩 / 扩散动画的固定端点。
	 *
	 * light → dark：从左下角收缩到点击位置
	 * dark → light：从点击位置扩散到左下角
	 */
	const leftBottomX = 0
	const leftBottomY = window.innerHeight

	/**
	 * ========================================
	 * 3. 最大半径
	 * ========================================
	 *
	 * 左下角到右上角的距离，保证以左下角为圆心的圆
	 * 能完全覆盖整个页面。
	 */
	const maxRadius = Math.hypot(window.innerWidth, window.innerHeight)

	/**
	 * ========================================
	 * 4. 动画两端
	 * ========================================
	 *
	 * 左下角大圆：覆盖整个页面（动画的固定端）
	 * 点击处小圆：半径为 0（动画的点击端）
	 */
	const leftBottomClipPath = `circle(${maxRadius}px at ${leftBottomX}px ${leftBottomY}px)`

	const clickClipPath = `circle(0px at ${x}px ${y}px)`

	/**
	 * ========================================
	 * 5. 浏览器不支持 View Transition
	 * ========================================
	 */
	if (!('startViewTransition' in document)) {
		appStore.toggleDark()
		return
	}

	/**
	 * ========================================
	 * 6. 根据方向切换 z-index
	 * ========================================
	 *
	 * light → dark（收缩）：
	 *   旧页面 light 位于上层，从左下角大圆收缩到点击处，
	 *   露出下层的新页面 dark。
	 *
	 * dark → light（扩散）：
	 *   新页面 light 位于上层（默认层级），从点击处扩散到左下角，
	 *   覆盖下层的旧页面 dark。
	 *
	 * 连点时先跳过上一次还没跑完的 transition，
	 * 类名也按本次方向整体覆盖（而不是只在收缩时 add），
	 * 避免上一次遗留的 theme-transition-shrink 把本次层级关系搞反。
	 */
	currentTransition?.skipTransition()

	// 上一次切换遗留的动画会污染本次的伪元素，先清理
	cancelViewTransitionAnimations()

	document.documentElement.classList.toggle('theme-transition-shrink', !isDarkNow)

	/**
	 * ========================================
	 * 7. 创建 View Transition
	 * ========================================
	 */
	const transition = document.startViewTransition(async () => {
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
	 * 8. View Transition ready
	 * ========================================
	 */
	transition.ready
		.then(() => {
			/**
			 * ========================================
			 * ☀️ light → 🌙 dark（收缩）
			 * ========================================
			 *
			 * old = light（上层）
			 * new = dark（下层）
			 *
			 * 旧页面 light 从左下角大圆收缩到点击处小圆，
			 * 逐渐露出下层的新页面 dark。
			 *
			 *     ◻️ light（覆盖全屏）
			 *       ↓ 收缩
			 *      ●（点击处）
			 *     dark 显露
			 */
			if (!isDarkNow) {
				const animation = document.documentElement.animate(
					{
						clipPath: [leftBottomClipPath, clickClipPath]
					},
					{
						duration: transitionDuration,
						easing: 'ease-out',
						fill: 'forwards',
						pseudoElement: '::view-transition-old(root)'
					}
				)

				watchThemeAnimation(animation)

				return
			}

			/**
			 * ========================================
			 * 🌙 dark → ☀️ light（扩散）
			 * ========================================
			 *
			 * old = dark（下层）
			 * new = light（上层）
			 *
			 * 新页面 light 从点击处小圆扩散到左下角大圆，
			 * 逐渐覆盖下层的旧页面 dark。
			 *
			 *      ●（点击处）
			 *       ↓ 扩散
			 *     ◻️ light（覆盖全屏）
			 *     dark 被遮盖
			 */
			const animation = document.documentElement.animate(
				{
					clipPath: [clickClipPath, leftBottomClipPath]
				},
				{
					duration: transitionDuration,
					easing: 'ease-out',
					fill: 'forwards',
					pseudoElement: '::view-transition-new(root)'
				}
			)

			watchThemeAnimation(animation)
		})
		.catch(error => {
			// 连点时被 skipTransition 接管的切换，ready 会以 AbortError 结算，属预期内
			if ((error as Error)?.name !== 'AbortError') {
				console.error('❌ ViewTransition ready error:', error)
			}
		})

	/**
	 * ========================================
	 * 9. View Transition 整体完成
	 * ========================================
	 *
	 * 被新一轮切换接管时，上一次的 finished 同样会结算，
	 * 此时不做清理，避免抹掉本次刚设置好的类名与动画。
	 */
	const cleanupThemeTransition = () => {
		if (currentTransition !== transition) return

		currentTransition = null

		document.documentElement.classList.remove('theme-transition-shrink')

		cancelViewTransitionAnimations()
	}

	transition.finished
		.then(cleanupThemeTransition)
		.catch(error => {
			cleanupThemeTransition()
			console.error('❌ ViewTransition finished error:', error)
		})
}
</script>

<style>
/**
 * ============================================================
 * View Transition
 * ============================================================
 *
 * 禁用浏览器默认的 View Transition 动画。
 *
 * 完全由 clip-path 控制主题切换效果。
 */

::view-transition-group(root),
::view-transition-image-pair(root),
::view-transition-old(root),
::view-transition-new(root) {
	animation: none !important;
}

/**
 * ============================================================
 * 默认层级（dark → light 扩散）
 * ============================================================
 *
 * 新页面位于上层，从点击处扩散到左下角，
 * 覆盖下层的旧页面。
 */
::view-transition-old(root) {
	z-index: 1;
	mix-blend-mode: normal;
}

::view-transition-new(root) {
	z-index: 2;
	mix-blend-mode: normal;
}

/**
 * ============================================================
 * 收缩模式层级（light → dark 收缩）
 * ============================================================
 *
 * 旧页面位于上层，从左下角收缩到点击处，
 * 露出下层的新页面。
 */
:root.theme-transition-shrink::view-transition-old(root) {
	z-index: 2;
}

:root.theme-transition-shrink::view-transition-new(root) {
	z-index: 1;
}

/**
 * ============================================================
 * 减少动画
 * ============================================================
 */

@media (prefers-reduced-motion: reduce) {
	::view-transition-group(root),
	::view-transition-image-pair(root),
	::view-transition-old(root),
	::view-transition-new(root) {
		animation: none !important;
	}
}
</style>
