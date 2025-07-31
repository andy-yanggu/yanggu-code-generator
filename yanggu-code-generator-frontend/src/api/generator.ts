import service from '@/utils/request'
import qs from 'qs'
import { downloadFile } from '@/utils/download'

//预览代码
export const generatorPreviewApi = (dataForm: any) => {
	return service.post('/generator/preview', dataForm)
}

//下载单文件
export const generatorDownloadSingleApi = (dataForm: any) => {
	const url = '/generator/downloadSingle?' + qs.stringify(dataForm)
	return downloadFile(url)
}

//生成单文件到本地
export const generatorSingleLocalApi = (dataForm: any) => {
	return service.post('/generator/singleLocal', dataForm)
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

//表生成代码（zip压缩包）
export const generatorTableDownloadZipApi = (dataForm: any) => {
	const url = '/generator/table/downloadZip?' + qs.stringify(dataForm)
	return downloadFile(url)
}

//表下载到本地
export const generatorTableDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/table/downloadLocal', dataForm)
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
