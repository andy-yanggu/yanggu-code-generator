import service from '@/utils/request'

//提交表单
export const projectSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/project/update', dataForm)
	} else {
		//新增接口
		return service.post('/project/add', dataForm)
	}
}

//删除接口
export const projectDeleteApi = (id: number) => {
	return service.delete('/project/delete?id=' + id)
}

//批量删除
export const projectDeleteListApi = (idList: Array<number>) => {
	return service.delete('/project/deleteList', { data: idList })
}

//查询详情
export const projectDetailApi = (id: number) => {
	return service.get('/project/detail?id=' + id)
}

//批量查询
export const projectDetailListApi = (idList: Array<number>) => {
	return service.post('/project/detailList', idList)
}

//简单分页
export const projectEntityPageApi = (queryForm: any) => {
	return service.post('/project/entityPage', queryForm)
}

//简单列表
export const projectEntityListApi = (queryForm: any) => {
	return service.post('/project/entityList', queryForm)
}

//复杂分页
export const projectVOPageApi = (queryForm: any) => {
	return service.post('/project/voPage', queryForm)
}

//复杂列表
export const projectVOListApi = (queryForm: any) => {
	return service.post('/project/voList', queryForm)
}

export const projectTableListApi = (queryForm: any) => {
	return service.post('/project/tableList', queryForm)
}
