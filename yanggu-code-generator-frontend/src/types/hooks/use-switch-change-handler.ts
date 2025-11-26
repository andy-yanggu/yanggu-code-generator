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
export interface SwitchState<T = any> {
	// 字段
	field: string
	// 数据列表
	states: [SwitchOption<T>, SwitchOption<T>]
	// 确认提示
	confirmText?: (newText: string) => string
	// 成功提示
	successText?: (oldText: string, newText: string) => string
}
/**
 * useSwitchState 返回值的标准数据结构
 * 用于描述开关组件切换时的状态、文案、逻辑方法等
 */
export interface SwitchStateResult<T = any> {
	/**
	 * 绑定的字段名（对应表格行里的字段，如 "status"、"enable"）
	 */
	field: string

	/**
	 * 开关当前激活（true）的值
	 */
	activeValue: T

	/**
	 * 开关当前非激活（false）的值
	 */
	inactiveValue: T

	/**
	 * 激活状态对应的文本描述（如 “启用”）
	 */
	activeText: string

	/**
	 * 非激活状态对应的文本描述（如 “禁用”）
	 */
	inactiveText: string

	/**
	 * 值与文本描述的映射表
	 * 如：{ true: "启用", false: "禁用" }
	 */
	text: Record<T, string>

	/**
	 * 根据新的值计算旧值，用于 UI 回滚
	 * 比如 newValue=true → oldValue=false
	 */
	getOldValue: (newValue: T) => T

	/**
	 * 生成确认提示文案
	 * 如：“确定要修改为【启用】吗？”
	 */
	confirmMessage: (newText: string) => string

	/**
	 * 生成成功提示文案
	 * 如：“已从【禁用】修改为【启用】”
	 */
	successMessage: (oldText: string, newText: string) => string
}

/**
 * Switch 状态更新统一配置接口
 */
export interface SwitchUpdateConfig<T = any> {
	/**
	 * 开关状态配置（useSwitchState 的返回值）
	 */
	switchState: SwitchStateResult<T>

	/**
	 * 真正执行业务更新的接口函数
	 * newValue：新的开关值
	 * row：当前行数据
	 */
	apiFn: (newValue: T, row: any) => Promise<any>

	/**
	 * 请求成功后的回调，可选
	 */
	afterSuccess?: () => void
}
