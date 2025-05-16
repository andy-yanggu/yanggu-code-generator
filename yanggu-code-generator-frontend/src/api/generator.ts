import service from '@/utils/request'
import qs from 'qs'

//表预览代码
export const generatorTablePreviewApi = (tableId: number) => {
	return service.get('/generator/table/preview?tableId=' + tableId)
}

//表下载单文件
export const generatorTableDownloadSingleApi = (dataForm: any) => {
	location.href = import.meta.env.VITE_API_URL + '/generator/table/downloadSingle?' + qs.stringify(dataForm)
}

//表生成代码（zip压缩包）
export const generatorTableDownloadZipApi = (dataForm: any) => {
	location.href = import.meta.env.VITE_API_URL + '/generator/table/downloadZip?' + qs.stringify(dataForm)
}

//表下载到本地
export const generatorTableDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/table/downloadLocal', dataForm)
}

//项目预览代码
export const generatorProjectPreviewApi = (projectId: number) => {
	return service.get('/generator/project/preview?projectId=' + projectId)
}

//项目树形数据
export const generatorProjectTreeDataApi = (projectId: number) => {
	return service.get('/generator/project/treeData?projectId=' + projectId)
}

//项目下载zip压缩包
export const generatorProjectDownloadZipApi = (dataForm: any) => {
	location.href = import.meta.env.VITE_API_URL + '/generator/project/downloadZip?' + qs.stringify(dataForm)
}

//项目下载到本地
export const generatorProjectDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/project/downloadLocal', dataForm)
}

//项目下载单文件
export const generatorProjectDownloadSingleApi = (dataForm: any) => {
	location.href = import.meta.env.VITE_API_URL + '/generator/project/downloadSingle?' + qs.stringify(dataForm)
}
