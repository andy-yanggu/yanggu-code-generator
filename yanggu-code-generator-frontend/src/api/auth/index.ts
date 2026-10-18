import type { LoginVO, TokenInfo } from '@/types'
import { service } from '@/utils/request'

const baseUrl: string = '/auth'

/**
 * 登录、权限 API
 *
 * 每个方法都必须写显式返回类型：utils/request.ts 的响应拦截器（L72）已经把后端的
 * R<T> 拆开、直接 resolve 出 result.data，但 axios 自身的声明仍然是 Promise<AxiosResponse>。
 * 写上 Promise<LoginVO> 后，axios 方法的 R 泛型会从上下文反推成 LoginVO，
 * 类型层与运行时行为就对齐了（与 createCrudApi 靠 CrudApi 接口收口的做法一致）。
 */
export const authApi = {
	// 登录
	login: (dataForm: any): Promise<LoginVO> => service.post(`${baseUrl}/login`, dataForm),

	// 注册（后端暂未实现 /auth 接口，返回形状按「注册即登录」约定与 login 保持一致）
	register: (dataForm: any): Promise<LoginVO> => service.post(`${baseUrl}/register`, dataForm),

	// 刷新token（同上，按 R<TokenInfo> 约定标注）
	refreshToken: (refreshToken: string): Promise<TokenInfo> => service.get(`${baseUrl}/refreshToken`, { params: { refreshToken } }),

	// 退出登录
	logout: (): Promise<void> => service.post(`${baseUrl}/logout`)
}
