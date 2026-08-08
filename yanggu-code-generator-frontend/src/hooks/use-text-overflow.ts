import type { Ref } from 'vue'

/**
 * 文本溢出检测hook
 * @param text 文本内容
 * @param maxWidth 最大宽度
 */
export const useTextOverflow = (text?: Ref<string>, maxWidth?: Ref<string>) => {
	const textRef = ref()
	const isOverflow = ref(false)

	// 检查文本是否溢出
	const checkOverflow = () => {
		nextTick(() => {
			// 获取文本元素
			const element = textRef.value.$el || textRef.value
			if (element) {
				isOverflow.value = element.scrollWidth > element.offsetWidth
				// console.log('element.scrollWidth:', element.scrollWidth, 'element.offsetWidth:', element.offsetWidth)
			}
		})
	}

	// 监听文本和最大宽度的变化
	if (text || maxWidth) {
		watch(
			[() => text?.value, () => maxWidth?.value],
			() => {
				checkOverflow()
			},
			{ immediate: true }
		)
	}

	// 初始化时检查文本是否溢出
	onMounted(checkOverflow)

	// 使用VueUse的useResizeObserver自动监听尺寸变化
	useResizeObserver(textRef, checkOverflow)

	return {
		textRef,
		isOverflow,
		checkOverflow
	}
}
