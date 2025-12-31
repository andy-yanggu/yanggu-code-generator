import { downloadFile, service } from '@/utils/request'
import { AxiosRequestConfig } from 'axios'

const baseUrl: string = '/gen/generator'

// 生成代码API
export const genGeneratorApi = {
	// 预览代码
	preview: (dataForm: any): Promise<any> => {
		return service.post(baseUrl + '/preview', dataForm)
	},
	// 下载单文件
	downloadSingle: (dataForm: any) => {
		return downloadFile(baseUrl + '/downloadSingle', dataForm)
	},
	// 生成单文件到本地
	singleLocal: (dataForm: any) => {
		return service.post(baseUrl + '/singleLocal', dataForm)
	},
	// 项目下载zip压缩包
	projectDownloadZip: (dataForm: any, noErrorMessage = false) => {
		return downloadFile(baseUrl + '/project/downloadZip', dataForm, noErrorMessage)
	},
	// 项目下载到本地
	projectDownloadLocal: (dataForm: any) => {
		return service.post(baseUrl + '/project/downloadLocal', dataForm)
	},
	// 表生成代码（zip压缩包）
	tableDownloadZip: (dataForm: any) => {
		return downloadFile(baseUrl + '/table/downloadZip', dataForm)
	},
	// 表下载到本地
	tableDownloadLocal: (dataForm: any) => {
		return service.post(baseUrl + '/table/downloadLocal', dataForm)
	},
	// 枚举生成代码（zip压缩包）
	enumDownloadZip: (dataForm: any) => {
		return downloadFile(baseUrl + '/enum/downloadZip', dataForm)
	},
	// 枚举下载到本地
	enumDownloadLocal: (dataForm: any) => {
		return service.post(baseUrl + '/enum/downloadLocal', dataForm)
	},
	// 模板测试
	templateTest: (queryForm: any, config?: AxiosRequestConfig): Promise<any> => {
		return service.post(baseUrl + '/template/test', queryForm, config)
	}
}
