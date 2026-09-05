/**
 * MSW Mock 共享工具
 *
 * 提供自动拼接 baseUrl 的 http 实例和统一响应构造函数。
 */

import { http as mswHttp, HttpResponse } from 'msw'
import { env } from '@/config'

// ============================================================
// 自动拼接 baseUrl 的 http 实例
// ============================================================

/** 自动拼接 env.apiUrl 的 http 实例，用法与 msw http 完全一致 */
export const http = new Proxy(mswHttp, {
	get(target, prop: string) {
		const original = target[prop as keyof typeof target]
		if (typeof original !== 'function') return original
		return (url: string, ...args: any[]) =>
			(original as Function).call(target, `${env.apiUrl}${url}`, ...args)
	}
})

// ============================================================
// 统一响应构造函数（与后端 Result<T> 结构一致）
// ============================================================

/** 统一成功响应 */
export const ok = <T>(data: T) => HttpResponse.json({ code: 200, message: 'success', data })

/** 空成功响应 */
export const okVoid = () => HttpResponse.json({ code: 200, message: 'success', data: null })
