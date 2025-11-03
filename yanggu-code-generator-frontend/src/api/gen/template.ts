import { service } from '@/utils/request'
import { createCrudApi, CrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'

// 模板管理Entity
export interface GenTemplateEntity {
	// 主键ID
	id: number
	// 模板组ID
	templateGroupId: number
	// 模板名称
	templateName: string
	// 父级ID
	parentId: number
	// 描述
	templateDesc: string
	// 文件或者目录名称
	fileName: string
	// 模板内容
	templateContent: string
	// 模板类型（0-目录，1-模板文件，2-二进制文件）
	templateType: number
	// 二进制原始文件名
	binaryOriginalFileName: string
	// 模板路径
	templatePath?: string
}

// 模板管理Query查询参数
export interface GenTemplateQuery extends PageQuery {
	// 模板组ID
	templateGroupId?: number
	// 模板名称
	templateName?: string
	// 模板类型
	templateType?: number
	// 模板组类型
	templateGroupType?: number
	// 模板组ID列表
	templateGroupIdList?: number[]
	// 模板组名称
	templateGroupName?: string
	// 文件名称
	fileName?: string
}

// 特定api
interface CustomApi {
	detail: (queryForm: any) => Promise<any>
	treeData: (templateGroupId: number) => Promise<any[]>
	updateContent: (dataForm: any) => Promise<void>
	updateParent: (dataForm: any) => Promise<void>
	updateContentList: (dataFormList: any[]) => Promise<void>
}

const baseUrl: string = '/gen/template'

// 模板管理API
export const genTemplateApi: CrudApi<GenTemplateEntity, GenTemplateQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 查询详情
	detail: (queryForm: any): Promise<any> => {
		return service.get(baseUrl + '/detail', { params: queryForm })
	},
	// 树形数据
	treeData: (templateGroupId: number): Promise<any[]> => {
		return service.get(baseUrl + '/tree', { params: { templateGroupId } })
	},
	// 修改模板内容
	updateContent: (dataForm: any) => {
		return service.put(baseUrl + '/updateContent', dataForm)
	},
	// 修改模板父级
	updateParent: (dataForm: any) => {
		return service.put(baseUrl + '/updateParent', dataForm)
	},
	// 批量修改模板内容
	updateContentList: (dataFormList: any[]) => {
		return service.put(baseUrl + '/updateContentBatch', dataFormList)
	}
}
