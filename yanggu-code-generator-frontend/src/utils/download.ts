import { ElMessage } from 'element-plus'
import service from '@/utils/request'

/**
 * 统一文件下载工具
 *
 * @param url 请求地址
 */
export const downloadFile = (url: string): Promise<void> => {
	return new Promise((resolve, reject) => {
		service
			.get(url, {
				responseType: 'blob'
			})
			.then((response: any) => {
				// 提取文件名
				const contentDisposition = response.headers['content-disposition']
				let filename = ''

				if (contentDisposition) {
					const matches = /filename="?(.+)"?/.exec(contentDisposition)
					if (matches && matches[1]) {
						filename = matches[1]
					}
				}

				// 创建下载链接并触发下载
				const downloadUrl = window.URL.createObjectURL(new Blob([response.data]))
				const a = document.createElement('a')
				a.href = downloadUrl
				a.download = filename
				document.body.appendChild(a)
				a.click()
				window.URL.revokeObjectURL(downloadUrl)
				document.body.removeChild(a)

				// 成功时直接resolve，不返回true
				resolve()
			})
			.catch((error: any) => {
				console.error('导出失败:', error)
				let errorMessage = '导出失败，请稍后重试或联系管理员'

				if (error.response) {
					errorMessage = `导出失败，服务器返回状态码：${error.response.status}`
				} else if (error.request) {
					errorMessage = '导出失败，请检查网络连接或稍后重试'
				}

				ElMessage.error(errorMessage)
				// 失败时reject错误信息
				reject(new Error(errorMessage))
			})
	})
}
