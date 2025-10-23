import { service } from '@/utils/request'

const baseUrl: string = '/auth'

// 登录、权限API
export const authApi = {
	// 登录
	login: (dataForm: any) => service.post(`${baseUrl}/login`, dataForm),

	// 注册
	register: (dataForm: any) => service.post(`${baseUrl}/register`, dataForm),

	// 刷新token
	refreshToken: (refreshToken: string) => service.get(`${baseUrl}/refreshToken`, { params: { refreshToken } }),

	// 退出登录
	logout: () => service.post(`${baseUrl}/logout`)
}
