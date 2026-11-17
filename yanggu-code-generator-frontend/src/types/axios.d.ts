import 'axios'

declare module 'axios' {
	export interface AxiosRequestConfig {
		/**
		 * 是否禁止全局错误提示
		 * - 不设置 或 false：显示错误提示（默认行为）
		 * - true：不显示错误提示。自行处理错误
		 */
		noErrorMessage?: boolean

		/**
		 * 是否返回完整后端响应（raw = 未经 unwrap 的原始外层）
		 * - 不设置 或 false：返回 res.data（默认行为）
		 * - true：返回完整 res（含 code/message/data 的 Result 对象）
		 */
		rawResponse?: boolean
	}
}
