import axios from 'axios'
import qs from 'qs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store'
import { router } from '@/router'
import { Result } from '@/types/api/common'
import { useRoute } from 'vue-router'

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

		// 添加token
		const userStore = useUserStore()
		if (userStore.tokenInfo) {
			config.headers[userStore.tokenInfo.tokenName] = userStore.tokenInfo.accessToken
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
			return handleHttpStatusError(status, statusText, config)
		}

		// 文件下载直接返回
		if (config.responseType === 'blob') {
			const contentType = response.headers['content-type']
			const disposition = response.headers['content-disposition']

			const isJsonError = contentType?.includes('application/json') || !disposition
			// 后端接口报错
			if (isJsonError) {
				return parseBlobError(response, config)
			} else {
				// 正常文件下载
				return response
			}
		}

		const result = data as Result

		// 响应成功
		if (result.code === 200) {
			return config.rawResponse === false ? result : result.data
		}

		// 处理业务逻辑错误
		return handleBusinessError(result, config)
	},
	error => {
		const config = error.config || {}
		return handleNetworkError(error, config)
	}
)

/**
 * 解析 blob 中的 JSON 错误信息
 * 适用于：下载接口 responseType=blob，但后端返回 application/json
 */
const parseBlobError = async (response: any, config: any) => {
	try {
		const blob = response.data

		// 有些浏览器/场景下 blob 可能为空
		if (!blob || blob.size === 0) {
			if (!config.noErrorMessage) {
				ElMessage.error('文件下载失败，返回内容为空')
			}
			return Promise.reject(new Error('Blob 内容为空'))
		}

		// blob → text → json
		const text = await blob.text()
		const result = JSON.parse(text) as Result

		// 复用你现有的业务错误处理
		return handleBusinessError(result, config)
	} catch (e) {
		if (!config.noErrorMessage) {
			ElMessage.error('文件下载失败')
		}
		return Promise.reject(new Error('文件下载失败'))
	}
}

/**
 * 处理HTTP状态码错误
 */
const handleHttpStatusError = (status: number, statusText: string, config: any) => {
	if (!config.noErrorMessage) {
		ElMessage.error(statusText || '网络错误')
	}
	return Promise.reject(new Error(statusText || '网络错误'))
}

/**
 * 处理业务逻辑错误
 */
const handleBusinessError = (result: Result, config: any) => {
	// 未认证
	if (result.code === 401) {
		// 重定向到登录页面
		ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
			confirmButtonText: '重新登录',
			cancelButtonText: '取消',
			type: 'warning'
		}).then(() => {
			const userStore = useUserStore()
			userStore.clearAll()
			// 使用 useRoute 获取当前路由信息，保持与其他组件一致
			const route = useRoute()
			router.replace({
				path: '/auth/login',
				query: {
					redirect: encodeURIComponent(route.fullPath || '/')
				}
			})
		})
		return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
	} else if (result.code === 403) {
		// 未授权
		if (!config.noErrorMessage) {
			ElMessage.error('您没有权限访问该资源')
		}
		return Promise.reject('您没有权限访问该资源')
	} else {
		// 业务错误提示：只有没有设置 noErrorMessage 才提示
		if (!config.noErrorMessage) {
			ElMessage.error(result.message || '请求错误')
		}
		return Promise.reject(result)
	}
}

/**
 * 处理网络请求错误
 */
const handleNetworkError = (error: any, config: any) => {
	if (!config.noErrorMessage) {
		// 根据错误类型提供更具体的错误信息
		let errorMessage = '请求失败'
		if (error.code === 'ECONNABORTED') {
			errorMessage = '请求超时，请稍后重试'
		} else if (error.message === 'Network Error') {
			errorMessage = '网络连接失败，请检查网络设置'
		} else if (error.response) {
			switch (error.response.status) {
				case 400:
					errorMessage = '请求参数错误'
					break
				case 401:
					errorMessage = '未授权，请重新登录'
					break
				case 403:
					errorMessage = '拒绝访问'
					break
				case 404:
					errorMessage = '请求资源不存在'
					break
				case 408:
					errorMessage = '请求超时'
					break
				case 500:
					errorMessage = '服务器内部错误'
					break
				case 501:
					errorMessage = '服务未实现'
					break
				case 502:
					errorMessage = '网关错误'
					break
				case 503:
					errorMessage = '服务不可用'
					break
				case 504:
					errorMessage = '网关超时'
					break
				default:
					errorMessage = `连接错误${error.response.status}`
			}
		} else if (error.request) {
			errorMessage = '服务器无响应，请稍后重试'
		}
		ElMessage.error(errorMessage)
	}
	return Promise.reject(error)
}

/**
 * 统一文件下载工具（适用于小文件）
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

				// 获取下载的文件名
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
				reject(error)
			})
	})
}

/**
 * 下载大文件（通过 a 标签 + token 拼接）
 * @param url 请求地址
 * @param params 请求参数
 */
export const downloadBigFile = (url: string, params?: Record<string, any>) => {
	return new Promise<void>(resolve => {
		const userStore = useUserStore()
		const tokenName = userStore.tokenInfo.tokenName
		const tokenValue = userStore.tokenInfo.accessToken
		const requestParam = { ...(params ?? {}), [tokenName]: tokenValue }

		// 拼接完整 URL
		const baseURL = import.meta.env.VITE_API_URL as string
		const downloadUrl = `${baseURL}${url}?${qs.stringify(requestParam, { indices: false })}`

		// 创建隐藏的 a 标签
		const a = document.createElement('a')
		a.href = downloadUrl
		a.style.display = 'none'
		document.body.appendChild(a)
		a.click()
		document.body.removeChild(a)
		resolve()
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
			// 进行一下非空判断，有些组件当没有数据时，会报错
			if (resp.data.size === 0) {
				return Promise.reject(new Error('Blob 内容为空'))
			}
			const blob = new Blob([resp.data], { type })
			return URL.createObjectURL(blob)
		})
		.catch(error => {
			let errorMessage = '加载blob数据失败，请稍后重试或联系管理员'
			if (!noErrorMessage) {
				if (error.response) {
					errorMessage = `加载blob数据失败，服务器返回状态码：${error.response.status}`
				} else if (error.request) {
					errorMessage = '加载blob数据失败，请检查网络连接或稍后重试'
				}
				ElMessage.error(errorMessage)
			}
			return Promise.reject(new Error(errorMessage))
		})
}
