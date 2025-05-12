import service from '@/utils/request'

//提交表单
export const datasourceSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/datasource/update', dataForm)
	} else {
		//新增接口
		return service.post('/datasource/add', dataForm)
	}
}

//删除接口
export const datasourceDeleteApi = (id: number) => {
	return service.delete('/datasource/delete?id=' + id)
}

//批量删除
export const datasourceDeleteListApi = (idList: Array<number>) => {
	return service.delete('/datasource/deleteList', { data: idList })
}

//查询详情
export const datasourceDetailApi = (id: number) => {
	return service.get('/datasource/detail?id=' + id)
}

//批量查询
export const datasourceDetailListApi = (idList: Array<number>) => {
	return service.post('/datasource/detailList', idList)
}

//简单分页
export const datasourceEntityPageApi = (queryForm: any) => {
	return service.post('/datasource/entityPage', queryForm)
}

//简单列表
export const datasourceEntityListApi = (queryForm: any) => {
	return service.post('/datasource/entityList', queryForm)
}

//复杂分页
export const datasourceVOPageApi = (queryForm: any) => {
	return service.post('/datasource/voPage', queryForm)
}

//复杂列表
export const datasourceVOListApi = (queryForm: any) => {
	return service.post('/datasource/voList', queryForm)
}
