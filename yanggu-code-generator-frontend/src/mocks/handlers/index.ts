/**
 * MSW Mock Handlers 统一入口
 *
 * 默认只启用认证 mock，其余接口放行到真实后端。
 * 需要 mock 的模块，取消对应行的注释即可。
 */

// 认证模块（默认启用）
import { authHandlers } from './auth'

export const handlers = [
	// ---- 认证（默认启用）----
	...authHandlers
]
