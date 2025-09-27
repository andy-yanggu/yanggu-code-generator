import { service } from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/template'

// 模板API
export const genTemplateApi = {
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
