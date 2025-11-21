/**
 * 开关数据
 */
export interface SwitchOption<T = any> {
	// 值
	value: T
	// 文本
	text: string
	// 是否激活
	isActive: boolean
}
/**
 * 开关状态切换数据
 */
export interface SwitchState {
	// 字段
	field: string
	// 数据列表
	states: SwitchOption[]
	// 确认提示
	confirmText?: (newText: string) => string
	// 成功提示
	successText?: (oldText: string, newText: string) => string
}
