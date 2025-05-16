import service from '@/utils/request'
import qs from 'qs'

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
	location.href = import.meta.env.VITE_API_URL + '/generator/table/downloadZip?' + qs.stringify(dataForm)
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
	location.href = import.meta.env.VITE_API_URL + '/generator/project/downloadZip?' + qs.stringify(dataForm)
}

export const generatorProjectDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/project/downloadLocal', dataForm)
}

//下载单文件
export const generatorProjectDownloadSingleApi = (dataForm: any) => {
	location.href = import.meta.env.VITE_API_URL + '/generator/project/download-template-content?' + qs.stringify(dataForm)
}
