import { nextTick, onMounted, Ref, ref, watch } from 'vue'
import { useResizeObserver } from '@vueuse/core'

/**
 * 文本溢出检测hook
 * @param text 文本内容
 * @param maxWidth 最大宽度
 */
export const useTextOverflow = (text: Ref<string>, maxWidth?: Ref<string>) => {
	// 这里使用el-text
	const textRef = ref()
	const isOverflow = ref(false)

	// 检查文本是否溢出
	const checkOverflow = () => {
		nextTick(() => {
			if (textRef.value?.$el) {
				const element = textRef.value.$el
				isOverflow.value = element.scrollWidth > element.offsetWidth
				console.log('element.scrollWidth:', element.scrollWidth, 'element.offsetWidth:', element.offsetWidth)
				console.log('isOverflow:', isOverflow.value)
			}
		})
	}

	// 监听文本和最大宽度的变化
	watch(() => [text, maxWidth], checkOverflow, { immediate: true })

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
