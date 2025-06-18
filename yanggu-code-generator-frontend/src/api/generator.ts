import service from '@/utils/request'
import qs from 'qs'
import { downloadFile } from '@/utils/download'

//表预览代码
export const generatorTablePreviewApi = (tableId: number) => {
	return service.get('/generator/table/preview?tableId=' + tableId)
}

//表下载单文件
export const generatorTableDownloadSingleApi = (dataForm: any) => {
	const url = '/generator/table/downloadSingle?' + qs.stringify(dataForm)
	return downloadFile(url)
}

//表生成代码（zip压缩包）
export const generatorTableDownloadZipApi = (dataForm: any) => {
	const url = '/generator/table/downloadZip?' + qs.stringify(dataForm)
	return downloadFile(url)
}

//表下载到本地
export const generatorTableDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/table/downloadLocal', dataForm)
}

//项目预览代码
export const generatorProjectPreviewApi = (projectId: number) => {
	return service.get('/generator/project/preview?projectId=' + projectId)
}

//项目下载zip压缩包
export const generatorProjectDownloadZipApi = (dataForm: any) => {
	const url = '/generator/project/downloadZip?' + qs.stringify(dataForm)
	return downloadFile(url)
}

//项目下载到本地
export const generatorProjectDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/project/downloadLocal', dataForm)
}

//项目下载单文件
export const generatorProjectDownloadSingleApi = (dataForm: any) => {
	const url = '/generator/project/downloadSingle?' + qs.stringify(dataForm)
	return downloadFile(url)
}

//枚举预览代码
export const generatorEnumPreviewApi = (enumId: number) => {
	return service.get('/generator/enum/preview?enumId=' + enumId)
}

//枚举下载zip压缩包
export const generatorEnumDownloadZipApi = (dataForm: any) => {
	const url = '/generator/enum/downloadZip?' + qs.stringify(dataForm)
	return downloadFile(url)
}

//枚举下载到本地
export const generatorEnumDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/enum/downloadLocal', dataForm)
}

//枚举下载单文件
export const generatorEnumDownloadSingleApi = (dataForm: any) => {
	const url = '/generator/enum/downloadSingle?' + qs.stringify(dataForm)
	return downloadFile(url)
}
