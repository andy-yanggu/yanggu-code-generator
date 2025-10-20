import axios from 'axios'
import qs from 'qs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user-store'
import { router } from '@/router'

// axios实例
export const service = axios.create({
	baseURL: import.meta.env.VITE_API_URL,
	timeout: 60000,
	headers: { 'Content-Type': 'application/json;charset=UTF-8' },
	paramsSerializer: params => qs.stringify(params, { indices: false }), // 使用qs.stringify进行序列化
	withCredentials: true // 允许携带cookie
})

// 请求拦截器
service.interceptors.request.use(
	config => {
		// 追加时间戳，防止GET请求缓存
		if (config.method?.toUpperCase() === 'GET') {
			config.params = { ...config.params, t: new Date().getTime() }
		}

		// 处理表单数据
		if (config.headers['Content-Type'] === 'application/x-www-form-urlencoded') {
			config.data = qs.stringify(config.data)
		}

		// 处理token
		const userStore = useUserStore()
		if (userStore.tokenInfo) {
			config.headers.Authorization = 'Bearer ' + userStore.tokenInfo.accessToken
		}

		return config
	},
	error => {
		return Promise.reject(error)
	}
)

// 响应拦截器
service.interceptors.response.use(
	response => {
		const { config, status, statusText, data } = response

		// 处理 HTTP 状态码错误
		if (status !== 200) {
			if (!config.noErrorMessage) {
				ElMessage.error(statusText || '网络错误')
			}
			return Promise.reject(new Error(statusText || '网络错误'))
		}

		// 文件下载直接返回
		if (config.responseType === 'blob') {
			return response
		}

		const res = data

		// 响应成功
		if (res.code === 200) {
			return config.rawResponse === false ? res : res.data
		}

		// 未认证
		if (res.code === 401) {
			// 重定向到登录页面
			ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
				confirmButtonText: '重新登录',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				const userStore = useUserStore()
				userStore.clearAll()
				router.replace({
					path: '/login',
					query: {
						redirect: encodeURIComponent(router.currentRoute.value.fullPath || '/')
					}
				})
			})
			return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
		} else if (res.code === 403) {
			// 未授权
			if (!config.noErrorMessage) {
				ElMessage.error('您没有权限访问该资源')
			}
			return Promise.reject('您没有权限访问该资源')
		}

		// 业务错误提示：只有没有设置 noErrorMessage 才提示
		if (!config.noErrorMessage) {
			ElMessage.error(res.message || '请求错误')
		}

		return Promise.reject(new Error(res.message || '请求错误'))
	},
	error => {
		const config = error.config || {}

		// 网络或请求错误提示
		if (!config.noErrorMessage) {
			ElMessage.error(error.message || '请求失败')
		}

		return Promise.reject(error)
	}
)

/**
 * 统一文件下载工具
 *
 * @param url 请求地址
 * @param params 请求参数
 * @param noErrorMessage 是否不显示错误提示
 */
export const downloadFile = (url: string, params?: any, noErrorMessage = false): Promise<void> => {
	return new Promise((resolve, reject) => {
		service
			.get(url, {
				responseType: 'blob',
				params,
				noErrorMessage
			})
			.then(response => {
				const contentDisposition = response.headers['content-disposition']
				let filename = 'download'

				if (contentDisposition) {
					const filenameStarMatch = contentDisposition.match(/filename\*=['"]?(?:UTF-8['"]?)?''?([^;]+)/i)
					const filenameMatch = contentDisposition.match(/filename=['"]?([^;]+)['"]?/i)
					filename = filenameStarMatch?.[1]
						? decodeURIComponent(filenameStarMatch[1])
						: filenameMatch?.[1]
							? decodeURIComponent(filenameMatch[1].replace(/\+/g, '%20'))
							: filename
				}

				const downloadUrl = URL.createObjectURL(new Blob([response.data]))
				const a = document.createElement('a')
				a.href = downloadUrl
				a.download = filename
				document.body.appendChild(a)
				a.click()
				window.URL.revokeObjectURL(downloadUrl)
				document.body.removeChild(a)

				resolve()
			})
			.catch(error => {
				if (!noErrorMessage) {
					let errorMessage = '导出失败，请稍后重试或联系管理员'
					if (error.response) {
						errorMessage = `导出失败，服务器返回状态码：${error.response.status}`
					} else if (error.request) {
						errorMessage = '导出失败，请检查网络连接或稍后重试'
					}
					ElMessage.error(errorMessage)
				}
				reject(error)
			})
	})
}

/**
 * 加载blob数据
 *
 * @param url 请求地址
 * @param type blob类型
 * @param params 请求参数
 * @param noErrorMessage 是否不显示错误提示
 */
export const loadBlob = (url: string, type: string, params?: any, noErrorMessage = false) => {
	return service
		.get(url, {
			responseType: 'blob',
			params,
			noErrorMessage
		})
		.then(resp => {
			const blob = new Blob([resp.data], { type })
			return URL.createObjectURL(blob)
		})
}
