import service from '@/utils/request'
import { downloadFile } from '@/utils/download'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/templateGroup'

export const genTemplateGroupApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 复制模板组
	copy: (dataForm: any) => {
		return service.post(baseUrl + '/copy', dataForm)
	},
	// 导出模板组
	export: (idList: number[]): Promise<void> => {
		return downloadFile(`${baseUrl}/export`, { idList })
	},
	// 导入模板组
	import: (dataForm: any) => {
		return service.post(baseUrl + '/import', dataForm, {
			headers: {
				'Content-Type': 'multipart/form-data'
			}
		})
	},
	// 获取级联数据
	cascaderData: (queryForm: any) => {
		return service.get(baseUrl + '/cascaderData', { params: queryForm })
	}
}
