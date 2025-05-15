import service from '@/utils/request'

//表预览代码
export const generatorTablePreviewApi = (dataForm: any) => {
	return service.post('/generator/table/preview', dataForm)
}

//表树形数据
export const generatorTableTreeDataApi = (dataForm: any) => {
	return service.post('/generator/table/treeData', dataForm)
}

//下载单文件
export const generatorTableDownloadApi = (dataForm: any) => {
	location.href =
		import.meta.env.VITE_API_URL + '/generator/table/download-template-content?tableId=' + dataForm.tableId + '&templateId=' + dataForm.templateId
}

// 生成代码（zip压缩包）
export const generatorTableDownloadZipApi = (dataForm: any) => {
	let dataFormString = '?'
	for (const key in dataForm) {
		const value = dataForm[key]
		if (value instanceof Array) {
			dataFormString += '&' + key + '=' + value.join(',')
		} else {
			dataFormString += '&' + key + '=' + dataForm[key]
		}
	}
	location.href = import.meta.env.VITE_API_URL + '/generator/table/downloadZip' + dataFormString
}

// 下载到本地
export const generatorTableDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/table/downloadLocal', dataForm)
}

// 项目预览代码
export const generatorProjectPreviewApi = (projectId: number) => {
	return service.get('/generator/project/preview?projectId=' + projectId)
}

// 项目树形数据
export const generatorProjectTreeDataApi = (projectId: number) => {
	return service.get('/generator/project/treeData?projectId=' + projectId)
}

export const generatorProjectDownloadZipApi = (dataForm: any) => {
	let dataFormString = '?'
	for (const key in dataForm) {
		const value = dataForm[key]
		if (value instanceof Array) {
			dataFormString += '&' + key + '=' + value.join(',')
		} else {
			dataFormString += '&' + key + '=' + dataForm[key]
		}
	}
	location.href = import.meta.env.VITE_API_URL + '/generator/project/downloadZip' + dataFormString
}

export const generatorProjectDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/project/downloadLocal', dataForm)
}