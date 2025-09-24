import service from '@/utils/request'

/**
 * 生成基础 CRUD API
 * @param baseUrl  例如 /gen/baseClass
 */
export function createCrudApi(baseUrl: string) {
	return {
		// 新增或修改
		submit: (dataForm: any) => (dataForm.id ? service.put(`${baseUrl}/update`, dataForm) : service.post(`${baseUrl}/add`, dataForm)),

		// 删除单个
		delete: (id: number) => service.delete(`${baseUrl}/delete`, { params: { id } }),

		// 批量删除
		deleteList: (idList: number[]) => service.delete(`${baseUrl}/deleteList`, { data: idList }),

		// 查询详情
		detail: (id: number) => service.get(`${baseUrl}/detail`, { params: { id } }),

		// 批量详情
		detailList: (idList: number[]) => service.post(`${baseUrl}/detailList`, idList),

		// 简单分页 / 列表
		entityPage: (queryForm?: any) => service.post(`${baseUrl}/entityPage`, queryForm || {}),
		entityList: (queryForm?: any) => service.post(`${baseUrl}/entityList`, queryForm || {}),

		// 复杂分页 / 列表
		voPage: (queryForm?: any) => service.post(`${baseUrl}/voPage`, queryForm || {}),
		voList: (queryForm?: any) => service.post(`${baseUrl}/voList`, queryForm || {})
	}
}
