import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenTableEntity, GenTableQuery, KeyArray } from '@/types'

// 特定api
interface CustomApi {
	// 导入表
	importData: (dataForm: any) => Promise<void>
	// 同步表
	sync: (id: number) => Promise<void>
	// 表批量生成代码检测
	generateCheck: (idList: KeyArray) => Promise<any>
}

const baseUrl: string = '/gen/table'

// 代码生成表API
export const genTableApi: EnabledCrudApi<GenTableEntity, GenTableQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenTableEntity, GenTableQuery>(baseUrl),
	// 导入表
	importData: (dataForm: any) => {
		return service.post(baseUrl + '/import', dataForm)
	},
	// 同步表
	sync: (id: number) => {
		return service.put(baseUrl + '/sync', {}, { params: { id } })
	},
	// 表批量生成代码检测
	generateCheck: (idList: KeyArray): Promise<any> => {
		return service.post(baseUrl + '/generateCheck', idList)
	}
}
