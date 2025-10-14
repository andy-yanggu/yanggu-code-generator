import { createCrudApi } from '@/api/common'
import { downloadFile, service } from '@/utils/request'

const baseUrl: string = '/gen/templateGroupProperty'

// 模板组属性API
export const genTemplateGroupPropertyApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 导出模板组属性
	export: (idList: number[]): Promise<void> => {
		return downloadFile(`${baseUrl}/export`, { idList })
	},
	// 导入模板组属性
	import: (dataForm: any) => {
		return service.post(baseUrl + '/import', dataForm, {
			headers: {
				'Content-Type': 'multipart/form-data'
			}
		})
	}
}
