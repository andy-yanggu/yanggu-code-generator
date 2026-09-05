/**
 * 认证模块 Mock Handlers
 *
 * 对应 API：src/api/auth/index.ts
 * 基础路径：/auth
 */

import { http, ok } from './_shared'

export const authHandlers = [
	// 登录
	http.post('/auth/login', () =>
		ok({
			userInfo: { username: 'admin', nickname: 'Mock用户', avatar: '', email: 'admin@qq.com', mobile: '18888888888' },
			tokenInfo: { tokenName: 'satoken', accessToken: 'mock-token-xxxx', refreshToken: 'mock-refresh-xxxx', expire: 86400 },
			menuList: [],
			permissionList: ['*'],
			roleList: ['admin']
		})
	)
]
