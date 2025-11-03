import { createCrudApi, CrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'
import { Key } from '@/types/common'

// 基类Entity
export interface GenBaseClassEntity {
	// 主键ID
	id: Key
	// 基类名称
	baseClassName: string
	// 基类包名
	packageName: string
	// 基类类名
	className: string
	// 基类字段
	fields: string
	// 备注
	remark: string
}

// 基类查询参数
export interface GenBaseClassQuery extends PageQuery {
	// 基类包名
	packageName?: string
	// 基类类名
	className?: string
}

export interface CustomApi {}

const baseUrl: string = '/gen/baseClass'

// 基类API
export const genBaseClassApi: CrudApi<GenBaseClassEntity, GenBaseClassQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenBaseClassEntity, GenBaseClassQuery>(baseUrl)
}
