import { createCrudApi, CrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'
import { LabelData } from '@/types/common'

// 模板组属性Entity
export interface GenTemplateGroupPropertyEntity {
	// 主键ID
	id: number | undefined
	// 模板组ID
	templateGroupId: number | undefined
	// 属性标题
	propTitle: string
	// 属性键
	propKey: string
	// 属性默认值
	propDefaultValue: string
	// 组件类型
	componentType: number | undefined
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
	templateGroupId: number | undefined
	// 属性标题
	propTitle: string
	// 属性键
	propKey: string
}

// 特定api
interface CustomApi {}

const baseUrl: string = '/gen/templateGroupProperty'

// 模板组属性API
export const genTemplateGroupPropertyApi: CrudApi<GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl)
}
