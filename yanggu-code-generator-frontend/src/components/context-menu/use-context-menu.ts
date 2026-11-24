import { ref } from 'vue'
import type { UseContextMenuReturn } from './types'

/**
 * 右键菜单状态与生命周期：仅持有原始光标坐标、显隐与当前 target，
 * 定位测量（flip+clamp）与点击外部/Esc 关闭由 <context-menu> 组件负责。
 */
export const useContextMenu = (): UseContextMenuReturn => {
	const visible = ref(false)
	const target = ref<any>(null)
	const position = ref({ x: 0, y: 0 })

	const open = (event: MouseEvent, nextTarget: any): void => {
		position.value = { x: event.clientX, y: event.clientY }
		target.value = nextTarget
		visible.value = true
	}

	const close = (): void => {
		visible.value = false
	}

	return { visible, target, position, open, close }
}
