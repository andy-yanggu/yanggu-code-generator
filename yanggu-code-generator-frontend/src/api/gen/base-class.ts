import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenBaseClassEntity, GenBaseClassQuery } from '@/types'

// 特定api
interface CustomApi {}

// 禁用的api
const disabled = [] as const

type ApiType = EnabledCrudApi<GenBaseClassEntity, GenBaseClassQuery, typeof disabled> & CustomApi

const baseUrl: string = '/gen/baseClass'

// 基类API
export const genBaseClassApi: ApiType = {
	// 通用CRUD接口
	...createCrudApi(baseUrl, disabled)
}
