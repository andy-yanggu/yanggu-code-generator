/**
 * MSW Mock Handlers 统一入口
 *
 * 默认只启用认证 mock，其余接口放行到真实后端。
 * 需要 mock 的模块，取消对应行的注释即可。
 */

// 认证模块（默认启用）
import { authHandlers } from './auth'
// 模板组/模板树模块（2026-11-25 Docker MySQL 不可用期间的临时 mock，已停用；需要时再取消注释）
// import { templateHandlers } from './template'

export const handlers = [
	// ---- 认证（默认启用）----
	...authHandlers,
	// ---- 模板树（临时全量 mock，后端已恢复，停用切回真接口）----
	// ...templateHandlers
]
