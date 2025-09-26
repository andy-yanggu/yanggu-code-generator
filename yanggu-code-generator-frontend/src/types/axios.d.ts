import 'axios'

declare module 'axios' {
	export interface AxiosRequestConfig {
		/**
		 * 是否禁止全局错误提示
		 */
		noErrorMessage?: boolean

		/**
		 * 是否返回完整后端响应
		 * - 不设置 或 true：返回 res.data（默认行为）
		 * - false：返回完整 res
		 */
		rawResponse?: boolean
	}
}
