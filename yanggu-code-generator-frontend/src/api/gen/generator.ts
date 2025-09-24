import service from '@/utils/request'
import qs from 'qs'
import { downloadFile } from '@/utils/download'

const baseUrl: string = '/gen/generator'

export const genGeneraorApi = {
	// 预览代码
	preview: (dataForm: any) => {
		return service.post(baseUrl + '/preview', dataForm)
	},
	// 下载单文件
	downloadSingle: (dataForm: any) => {
		const url = baseUrl + '/downloadSingle?' + qs.stringify(dataForm)
		return downloadFile(url)
	},
	// 生成单文件到本地
	singleLocal: (dataForm: any) => {
		return service.post(baseUrl + '/singleLocal', dataForm)
	},
	// 项目下载zip压缩包
	projectDownloadZip: (dataForm: any) => {
		const url = baseUrl + '/project/downloadZip?' + qs.stringify(dataForm)
		return downloadFile(url)
	},
	// 项目下载到本地
	projectDownloadLocal: (dataForm: any) => {
		return service.post(baseUrl + '/project/downloadLocal', dataForm)
	},
	// 表生成代码（zip压缩包）
	tableDownloadZip: (dataForm: any) => {
		const url = baseUrl + '/table/downloadZip?' + qs.stringify(dataForm)
		return downloadFile(url)
	},
	// 表下载到本地
	tableDownloadLocal: (dataForm: any) => {
		return service.post(baseUrl + '/table/downloadLocal', dataForm)
	},
	// 枚举生成代码（zip压缩包）
	enumDownloadZip: (dataForm: any) => {
		const url = baseUrl + '/enum/downloadZip?' + qs.stringify(dataForm)
		return downloadFile(url)
	},
	// 枚举下载到本地
	enumDownloadLocal: (dataForm: any) => {
		return service.post(baseUrl + '/enum/downloadLocal', dataForm)
	},
	// 模板测试
	templateTest: (queryForm: any) => {
		return service.post(baseUrl + '/template/test', queryForm)
	}
}
