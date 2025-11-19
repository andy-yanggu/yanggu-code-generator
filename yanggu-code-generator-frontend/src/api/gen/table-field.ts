import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'

// 代码生成表字段Entity
export interface GenTableFieldEntity {
	// id
	id: number
	// 表ID
	tableId: number
	// 字段名称
	fieldName: string
	// 字段类型
	fieldType: string
	// 字段说明
	fieldComment: string
	// 属性名
	attrName: string
	// 属性类型
	attrType: string
	// 属性包名
	packageName: string
	// 字段顺序
	fieldSort: number
	// 自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE
	autoFill: string
	// 主键 0：否  1：是
	primaryPk: boolean
	// 逻辑删除 0：否  1：是
	logicDelete: boolean
	// 逻辑删除值
	logicDeleteValue: string
	// 逻辑未删除值
	logicNotDeleteValue: string
	// 是否为字典 0：否  1：是
	dict: boolean
	// 字典值。key-value形式。key为字典值，value为展示值，使用"、"拼接
	dictValue: string
	// 表单项 0：否  1：是
	formItem: boolean
	// 表单必填 0：否  1：是
	formRequired: boolean
	// 表单类型
	formType: string
	// 表单字段顺序
	formFieldSort: number
	// 表单效验
	formValidator: string
	// 列表项 0：否  1：是
	gridItem: boolean
	// 列表排序 0：否  1：是
	gridSort: boolean
	// 列表字段顺序
	gridFieldSort: number
	// 查询项 0：否  1：是
	queryItem: boolean
	// 查询方式
	queryType: string
	// 查询字段顺序
	queryFieldSort: number
	// 查询表单类型
	queryFormType: string
}

// 代码生成表字段Query查询参数
export interface GenTableFieldQuery extends PageQuery {
	// 表ID
	tableId: number
}

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
