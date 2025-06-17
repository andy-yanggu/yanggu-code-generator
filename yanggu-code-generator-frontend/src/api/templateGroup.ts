import service from '@/utils/request'
import { ElMessage } from 'element-plus'

//提交表单
export const templateGroupSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/templateGroup/update', dataForm)
	} else {
		//新增接口
		return service.post('/templateGroup/add', dataForm)
	}
}

//删除接口
export const templateGroupDeleteApi = (id: number) => {
	return service.delete('/templateGroup/delete?id=' + id)
}

//批量删除
export const templateGroupDeleteListApi = (idList: Array<number>) => {
	return service.delete('/templateGroup/deleteList', { data: idList })
}

//查询详情
export const templateGroupDetailApi = (id: number) => {
	return service.get('/templateGroup/detail?id=' + id)
}

//批量查询
export const templateGroupDetailListApi = (idList: Array<number>) => {
	return service.post('/templateGroup/detailList', idList)
}

//简单分页
export const templateGroupEntityPageApi = (queryForm: any) => {
	return service.post('/templateGroup/entityPage', queryForm)
}

//简单列表
export const templateGroupEntityListApi = (queryForm: any) => {
	return service.post('/templateGroup/entityList', queryForm)
}

//复杂分页
export const templateGroupVOPageApi = (queryForm: any) => {
	return service.post('/templateGroup/voPage', queryForm)
}

//复杂列表
export const templateGroupVOListApi = (queryForm: any) => {
	return service.post('/templateGroup/voList', queryForm)
}

//复制模板组
export const copyTemplateApi = (dataForm: any) => {
	return service.post('/templateGroup/copy', dataForm)
}

//导出模板组
export const exportTemplateGroupApi = async (tableIdList: number[]) => {
	const listString = tableIdList.join(',')
	const url = `/templateGroup/export?idList=${listString}`

	try {
		const response = await service.get(url, {
			responseType: 'blob' //文件下载
		})

		// 提取文件名
		const contentDisposition = response.headers['content-disposition']
		let filename = 'templateGroups.json' // 默认文件名

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

		return true
	} catch (error: any) {
		console.error('导出失败:', error)

		if (error.response) {
			// 后端返回了错误状态码（如 400, 500）
			ElMessage.error(`导出失败，服务器返回状态码：${error.response.status}`)
		} else if (error.request) {
			// 请求已发出，但无响应（如网络中断）
			ElMessage.error('导出失败，请检查网络连接或稍后重试')
		} else {
			// 其他错误
			ElMessage.error('导出失败，请稍后重试或联系管理员')
		}
		return false
	}
}
