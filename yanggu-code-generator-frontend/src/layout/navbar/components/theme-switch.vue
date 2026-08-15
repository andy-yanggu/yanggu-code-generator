<template>
	<icon-button
		:el-icon="appStore.isDark ? Moon : Sunny"
		:tooltip="appStore.isDark ? '切换到日间模式' : '切换到夜间模式'"
		size="18px"
		@click="handleThemeToggle"
	></icon-button>
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
	 * 5. 调试日志
	 * ========================================
	 */
	console.group('🎨 Theme Switch')

	console.log('current theme:', isDarkNow ? 'dark' : 'light')

	console.log('target theme:', isDarkNow ? 'light' : 'dark')

	console.log('click position:', {
		x,
		y
	})

	console.log('left-bottom anchor:', {
		x: leftBottomX,
		y: leftBottomY
	})

	console.log('viewport:', {
		width: window.innerWidth,
		height: window.innerHeight
	})

	console.log('maxRadius:', maxRadius)

	console.log('left-bottom clipPath:', leftBottomClipPath)

	console.log('click clipPath:', clickClipPath)

	console.groupEnd()

	/**
	 * ========================================
	 * 6. 浏览器不支持 View Transition
	 * ========================================
	 */
	if (!('startViewTransition' in document)) {
		appStore.toggleDark()
		return
	}

	/**
	 * ========================================
	 * 7. 根据方向切换 z-index
	 * ========================================
	 *
	 * light → dark（收缩）：
	 *   旧页面 light 位于上层，从左下角大圆收缩到点击处，
	 *   露出下层的新页面 dark。
	 *
	 * dark → light（扩散）：
	 *   新页面 light 位于上层（默认层级），从点击处扩散到左下角，
	 *   覆盖下层的旧页面 dark。
	 */
	if (!isDarkNow) {
		document.documentElement.classList.add('theme-transition-shrink')
	}

	/**
	 * ========================================
	 * 8. 创建 View Transition
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

	/**
	 * ========================================
	 * 9. View Transition ready
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
						duration: 500,
						easing: 'ease-in-out',
						fill: 'forwards',
						pseudoElement: '::view-transition-old(root)'
					}
				)

				animation.finished.catch(error => {
					console.error('❌ Theme animation error:', error)
				})

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
					duration: 800,
					easing: 'ease-in-out',
					fill: 'forwards',
					pseudoElement: '::view-transition-new(root)'
				}
			)

			animation.finished.catch(error => {
				console.error('❌ Theme animation error:', error)
			})
		})
		.catch(error => {
			console.error('❌ ViewTransition ready error:', error)
		})

	/**
	 * ========================================
	 * 10. View Transition 整体完成
	 * ========================================
	 */
	transition.finished
		.then(() => {
			document.documentElement.classList.remove('theme-transition-shrink')
			console.log('✅ Theme transition finished')
		})
		.catch(error => {
			document.documentElement.classList.remove('theme-transition-shrink')
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
