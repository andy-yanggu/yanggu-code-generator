import type { Component, Ref } from 'vue'

/** 图标：字符串走 <svg-icon :icon>，组件走 <el-icon><component :is/></el-icon> */
export type IconSpec = string | Component

/** 静态值或按当前 target 求值的动态值 */
export type Dyn<T, R> = R | ((target: T) => R)

/**
 * 单个菜单项（Layer 2 schema）。label/icon 支持随 target 动态求值；
 * visible/disabled 以 target 判定；dividerBefore 仅当所依附项自身可见时才渲染分割线。
 */
export interface ContextMenuItem<T = any> {
	key: string
	label: Dyn<T, string>
	icon?: Dyn<T, IconSpec>
	dividerBefore?: boolean
	visible?: (target: T) => boolean
	disabled?: (target: T) => boolean
	onClick: (target: T) => void
}

/** useContextMenu 的返回契约：仅持有原始光标坐标与显隐/目标状态，定位测量由组件负责 */
export interface UseContextMenuReturn {
	visible: Ref<boolean>
	target: Ref<any>
	position: Ref<{ x: number; y: number }>
	open: (event: MouseEvent, target: any) => void
	close: () => void
}
