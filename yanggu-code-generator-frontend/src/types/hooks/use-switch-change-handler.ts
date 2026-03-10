/**
 * Switch 状态 + 行为的统一配置
 */
export interface SwitchUpdateConfig<T = any> {
	/**
	 * 绑定字段名（如status）
	 */
	switchField: string
	/**
	 * 确定字段中文名
	 */
	confirmFieldText: string

	/**
	 * 确定字段名
	 */
	confirmField: string

	/**
	 * 两个状态的定义（必须且只能两个）
	 */
	states: [
		{
			value: T
			text: string
			isActive: boolean
		},
		{
			value: T
			text: string
			isActive: boolean
		}
	]

	/**
	 * 确认提示文案
	 * newText：即将切换到的状态文本
	 */
	confirmText?: (confirmValue: string, newText: string) => string

	/**
	 * 成功提示文案
	 * oldText：原状态文本
	 * newText：新状态文本
	 */
	successText?: (confirmValue: string, oldText: string, newText: string) => string

	/**
	 * 真正执行业务更新的接口函数
	 */
	apiFn: (newValue: T, row: any) => Promise<any>

	/**
	 * 请求成功后的回调
	 */
	afterSuccess?: () => void
}
