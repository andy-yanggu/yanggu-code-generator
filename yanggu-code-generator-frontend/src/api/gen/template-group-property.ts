import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'
import { LabelData } from '@/types/common'
import { service } from '@/utils/request'

// 模板组属性Entity
export interface GenTemplateGroupPropertyEntity {
	// 主键ID
	id: number | string
	// 模板组ID
	templateGroupId: number | string
	// 属性标题
	propTitle: string
	// 属性键
	propKey: string
	// 属性默认值
	propDefaultValue: string | number | boolean
	// 组件类型
	componentType: number | string
	// 组件选项
	componentOptions: LabelData[]
	// 必填
	required: number
	// 排序
	propOrder: number
	// 备注
	remark: string
}

// 模板组属性Query查询参数
export interface GenTemplateGroupPropertyQuery extends PageQuery {
	// 模板组ID
	templateGroupId: number | string
	// 属性标题
	propTitle: string
	// 属性键
	propKey: string
}

// 特定api
interface CustomApi {
	// 变更是否必填
	changeRequired: (id: number | string, required: number) => Promise<void>
	// 变更排序
	changeOrder: (id: number | string, propOrder: number) => Promise<void>
}

const baseUrl: string = '/gen/templateGroupProperty'

// 模板组属性API
export const genTemplateGroupPropertyApi: EnabledCrudApi<GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 变更是否必填
	changeRequired: (id: number | string, required: number) => {
		return service.put(baseUrl + '/changeRequired', { id, required })
	},
	// 变更排序
	changeOrder: (id: number | string, propOrder: number) => {
		return service.put(baseUrl + '/changeOrder', { id, propOrder })
	}
}
