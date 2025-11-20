import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { GenTemplateEntity, GenTemplateQuery } from '@/types'

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
export const genTemplateApi: EnabledCrudApi<GenTemplateEntity, GenTemplateQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi<GenTemplateEntity, GenTemplateQuery>(baseUrl),
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
