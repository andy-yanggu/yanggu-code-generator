import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenTableFieldEntity, GenTableFieldQuery } from '@/types'

// 特定api
interface CustomApi {
	// 提交列表
	submitList: (dataFormList: GenTableFieldEntity[]) => Promise<void>
}

const baseUrl: string = '/gen/tableField'

// 代码生成表字段API
export const genTableFieldApi: EnabledCrudApi<GenTableFieldEntity, GenTableFieldQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenTableFieldEntity, GenTableFieldQuery>(baseUrl),
	// 提交列表
	submitList: (dataFormList: GenTableFieldEntity[]) => {
		return service.post(baseUrl + '/submitList', dataFormList)
	}
}
