import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { service } from '@/utils/request'
import { GenEnumEntity, GenEnumQuery, KeyArray } from '@/types'

// 特定api
interface CustomApi {
	// 枚举批量生成代码检测
	generateCheck: (idList: KeyArray) => Promise<any>
}

// 禁用的api
const disabled = [] as const

type ApiType = EnabledCrudApi<GenEnumEntity, GenEnumQuery, typeof disabled> & CustomApi

const baseUrl: string = '/gen/enum'

// 枚举API
export const genEnumApi: ApiType = {
	// 通用CRUD接口
	...createCrudApi<GenEnumEntity, GenEnumQuery>(baseUrl),
	// 枚举批量生成代码检测
	generateCheck: (idList: KeyArray) => {
		return service.post(baseUrl + '/generateCheck', idList)
	}
}
