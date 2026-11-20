import { downloadFile, service } from '@/utils/request'
import type { AxiosRequestConfig } from 'axios'
import type { GeneratorQuery, TemplateTestQuery } from '@/types'

const baseUrl: string = '/gen/generator'

// 生成代码API（6 端点全 POST，统一 GeneratorQuery 入参）
export const genGeneratorApi = {
	// 预览代码
	preview: (query: GeneratorQuery): Promise<any> => {
		return service.post(baseUrl + '/preview', query)
	},
	// 生成代码（zip压缩包）
	downloadZip: (query: GeneratorQuery, noErrorMessage = false) => {
		return downloadFile(baseUrl + '/downloadZip', query, noErrorMessage, 'post')
	},
	// 生成代码（服务器本地）
	downloadLocal: (query: GeneratorQuery): Promise<any> => {
		return service.post(baseUrl + '/downloadLocal', query)
	},
	// 下载单文件
	downloadSingle: (query: GeneratorQuery) => {
		return downloadFile(baseUrl + '/downloadSingle', query, false, 'post')
	},
	// 生成单文件到服务器本地
	singleLocal: (query: GeneratorQuery): Promise<any> => {
		return service.post(baseUrl + '/singleLocal', query)
	},
	// 模板测试
	templateTest: (query: TemplateTestQuery, config?: AxiosRequestConfig): Promise<any> => {
		return service.post(baseUrl + '/template/test', query, config)
	}
}