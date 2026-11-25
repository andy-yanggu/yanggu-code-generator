import type { App } from 'vue'

/**
 * 全局错误兜底：Vue errorHandler + window error/unhandledrejection
 *
 * 定位是“安全网”，只处理漏网错误，不介入 request.ts 拦截器已提示的正常业务错误。
 * 覆盖三类错误源：
 * - app.config.errorHandler：Vue 渲染/生命周期/watcher/事件处理器里的同步抛错
 * - window 'error'：非 Vue 上下文的脚本运行时错误
 * - window 'unhandledrejection'：未被 catch 的 Promise 拒绝（含拦截器 reject 冒泡）
 */

// 相同错误短时间窗去重，避免拦截器弹一次 + 兜底再弹一次
const DEDUP_WINDOW_MS = 1000
const shownSignatures = new Map<string, number>()

const isDuplicate = (signature: string): boolean => {
	const now = Date.now()
	const last = shownSignatures.get(signature)
	if (last && now - last < DEDUP_WINDOW_MS) {
		return true
	}
	shownSignatures.set(signature, now)
	return false
}

// 从 unknown 错误里取可读 message 与去重签名
const normalize = (err: unknown): { message: string; signature: string } => {
	if (err instanceof Error) {
		return { message: err.message, signature: `E:${err.message}` }
	}
	if (typeof err === 'string') {
		return { message: err, signature: `S:${err}` }
	}
	// 业务错误对象（拦截器 reject 的 Result）取 message 字段
	const msg = (err as { message?: string })?.message ?? ''
	return { message: msg || '系统异常，请稍后重试', signature: `O:${msg}` }
}

const report = (err: unknown, source: string): void => {
	const { message, signature } = normalize(err)
	// 控制台始终留全量堆栈，便于排查
	console.error(`[${source}]`, err)
	// 相同错误 1s 内只提示一次
	if (isDuplicate(signature)) {
		return
	}
	ElMessage.error(message)
}

export const registerGlobalErrorHandler = (app: App): void => {
	app.config.errorHandler = (err, _instance, info) => report(err, `vue:${info}`)

	window.addEventListener('error', event => report(event.error ?? event.message, 'window:error'))

	window.addEventListener('unhandledrejection', event => report(event.reason, 'unhandledrejection'))
}
