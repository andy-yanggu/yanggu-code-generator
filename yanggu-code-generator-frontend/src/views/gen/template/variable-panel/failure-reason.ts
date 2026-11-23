// 目录类接口的加载异常 → 面板可直接展示的原因文本；结构目录与带值目录两个 hook 共用
// 规格 6.2 ①：这两类接口都带 noErrorMessage，全局层不提示，原因只能由面板承载

/**
 * 把加载异常转成面板内可直接展示的原因文本
 *
 * request.ts 对业务错误 reject 的是 `{ code, message }`（message 是后端给的中文原因），
 * 对网络 / HTTP 错误 reject 的是 axios 原文（如 `Request failed with status code 500`），后者不能直接给用户看
 */
export const resolveFailureReason = (e: any): string => {
	// 401 / 403 走的是 Promise.reject('中文字符串')（request.ts:148 / request.ts:154），
	// 且本接口带 noErrorMessage 时这两类也不弹全局 toast，故原文必须原样透出给面板
	if (typeof e === 'string') {
		return e
	}
	if (typeof e?.code === 'number' && e.message) {
		return e.message
	}
	if (e?.code === 'ECONNABORTED') {
		return '请求超时，请稍后重试'
	}
	return '变量目录加载失败，请检查网络后重试'
}
