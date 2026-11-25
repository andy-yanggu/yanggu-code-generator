import type { App } from 'vue'

/**
 * 全局错误兜底：Vue errorHandler + window error/unhandledrejection
 *
 * 定位是“安全网”，只处理漏网错误，不介入 request.ts 拦截器已提示的正常业务错误。
 * 漏网错误只打印 console.error，不再弹 ElMessage，避免拦截器之外的误报干扰用户。
 * 覆盖三类错误源：
 * - app.config.errorHandler：Vue 渲染/生命周期/watcher/事件处理器里的同步抛错
 * - window 'error'：非 Vue 上下文的脚本运行时错误
 * - window 'unhandledrejection'：未被 catch 的 Promise 拒绝（含拦截器 reject 冒泡）
 */

const report = (err: unknown, source: string): void => {
	// 控制台留全量堆栈，便于排查；不再弹窗提示
	console.error(`[${source}]`, err)
}

export const registerGlobalErrorHandler = (app: App): void => {
	app.config.errorHandler = (err, _instance, info) => report(err, `vue:${info}`)

	window.addEventListener('error', event => report(event.error ?? event.message, 'window:error'))

	window.addEventListener('unhandledrejection', event => report(event.reason, 'unhandledrejection'))
}
