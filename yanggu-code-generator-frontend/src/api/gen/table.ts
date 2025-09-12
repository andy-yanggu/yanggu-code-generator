import service from '@/utils/request'

// 提交表单
export const tableSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/table/update', dataForm)
	} else {
		//新增接口
		return service.post('/table/add', dataForm)
	}
}

// 删除接口
export const tableDeleteApi = (id: number) => {
	return service.delete('/table/delete?id=' + id)
}

// 批量删除
export const tableDeleteListApi = (idList: number[]) => {
	return service.delete('/table/deleteList', { data: idList })
}

// 查询详情
export const tableDetailApi = (id: number) => {
	return service.get('/table/detail?id=' + id)
}

// 批量查询
export const tableDetailListApi = (idList: number[]) => {
	return service.post('/table/detailList', idList)
}

// 简单分页
export const tableEntityPageApi = (queryForm?: any) => {
	return service.post('/table/entityPage', queryForm ? queryForm : {})
}

// 简单列表
export const tableEntityListApi = (queryForm?: any) => {
	return service.post('/table/entityList', queryForm ? queryForm : {})
}

// 复杂分页
export const tableVOPageApi = (queryForm?: any) => {
	return service.post('/table/voPage', queryForm ? queryForm : {})
}

// 复杂列表
export const tableVOListApi = (queryForm?: any) => {
	return service.post('/table/voList', queryForm ? queryForm : {})
}

export const tableImportApi = (dataForm: any) => {
	return service.post('/table/import', dataForm)
}

//同步表数据
export const tableSyncApi = (id: number) => {
	return service.put('/table/sync?id=' + id)
}

//表批量生成代码检测
export const tableGenerateCheckApi = (idList: number[]) => {
	return service.post('/table/generateCheck', idList)
}
