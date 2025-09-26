import service from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/table'

export const genTableApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 导入表
	import: (dataForm: any) => {
		return service.post(baseUrl + '/import', dataForm)
	},
	// 同步表
	sync: (id: number) => {
		return service.put(baseUrl + '/sync', {}, { params: { id } })
	},
	// 表批量生成代码检测
	generateCheck: (idList: number[]): Promise<any> => {
		return service.post(baseUrl + '/generateCheck', idList)
	}
}
