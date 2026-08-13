<template>
	<icon-button
		:el-icon="appStore.isDark ? Moon : Sunny"
		:tooltip="appStore.isDark ? '切换到日间模式' : '切换到夜间模式'"
		size="18px"
		@click="handleThemeToggle"
	></icon-button>
</template>

<script setup lang="ts">
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
 * light → dark：
 * new(root) 从点击位置向外扩散
 *
 * dark → light：
 * new(root) 从全屏向点击位置收缩
 */
const handleThemeToggle = (event: MouseEvent) => {
	/**
	 * ========================================
	 * 1. 点击位置
	 * ========================================
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
	 * 2. 最大扩散半径
	 * ========================================
	 */
	const maxRadius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))

	const startClipPath = `circle(0px at ${x}px ${y}px)`

	const endClipPath = `circle(${maxRadius}px at ${x}px ${y}px)`

	/**
	 * ========================================
	 * 3. 日志
	 * ========================================
	 */
	console.group('🎨 Theme Switch')

	console.log('current theme:', isDarkNow ? 'dark' : 'light')

	console.log('x:', x)
	console.log('y:', y)

	console.log('viewport:', window.innerWidth, '×', window.innerHeight)

	console.log('maxRadius:', maxRadius)

	console.log('start:', startClipPath)
	console.log('end:', endClipPath)

	console.groupEnd()

	/**
	 * ========================================
	 * 4. 不支持 View Transition
	 * ========================================
	 */
	if (!('startViewTransition' in document)) {
		appStore.toggleDark()
		return
	}

	/**
	 * ========================================
	 * 5. 创建 View Transition
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
	 * 6. View Transition ready
	 * ========================================
	 */
	transition.ready
		.then(() => {
			/**
			 * ========================================
			 * light → dark
			 * ========================================
			 *
			 * 当前：
			 *
			 * old = light
			 * new = dark
			 *
			 * 让 dark：
			 *
			 * circle(0)
			 *      ↓
			 * circle(maxRadius)
			 *
			 * 从点击位置向外扩散。
			 */
			if (!isDarkNow) {
				const animation = document.documentElement.animate(
					{
						clipPath: [startClipPath, endClipPath]
					},
					{
						duration: 500,
						easing: 'ease-in-out',
						fill: 'forwards',
						pseudoElement: '::view-transition-new(root)'
					}
				)

				animation.finished.catch(error => {
					console.error('❌ Theme animation error:', error)
				})

				return
			}

			/**
			 * ========================================
			 * dark → light
			 * ========================================
			 *
			 * 当前：
			 *
			 * old = dark
			 * new = light
			 *
			 * 让 light：
			 *
			 * circle(maxRadius)
			 *      ↓
			 * circle(0)
			 *
			 * 从整个页面向点击位置收缩。
			 */
			const animation = document.documentElement.animate(
				{
					clipPath: [endClipPath, startClipPath]
				},
				{
					duration: 500,
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
	 * View Transition 整体完成
	 */
	transition.finished.catch(error => {
		console.error('❌ ViewTransition finished error:', error)
	})
}
</script>

<style>
/**
 * ============================================================
 * View Transition
 * ============================================================
 */

::view-transition-group(root),
::view-transition-image-pair(root),
::view-transition-old(root),
::view-transition-new(root) {
	animation: none !important;
}

/**
 * 旧页面
 */
::view-transition-old(root) {
	z-index: 1;
	mix-blend-mode: normal;
}

/**
 * 新页面
 */
::view-transition-new(root) {
	z-index: 2;
	mix-blend-mode: normal;
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
