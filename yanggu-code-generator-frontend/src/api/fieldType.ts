import service from '@/util/request'

//提交表单
export const fieldTypeSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/fieldType/update', dataForm)
	} else {
		//新增接口
		return service.post('/fieldType/add', dataForm)
	}
}

//删除接口
export const fieldTypeDeleteApi = (id: number) => {
	return service.delete('/fieldType/delete?id=' + id)
}

//批量删除
export const fieldTypeDeleteListApi = (idList: Array<number>) => {
	return service.delete('/fieldType/deleteList', { data: idList })
}

//查询详情
export const fieldTypeDetailApi = (id: number) => {
	return service.get('/fieldType/detail?id=' + id)
}

//批量查询
export const fieldTypeDetailListApi = (idList: Array<number>) => {
	return service.post('/fieldType/detailList', idList)
}

//简单分页
export const fieldTypeEntityPageApi = (queryForm: any) => {
	return service.post('/fieldType/entityPage', queryForm)
}

//简单列表
export const fieldTypeEntityListApi = (queryForm: any) => {
	return service.post('/fieldType/entityList', queryForm)
}

//复杂分页
export const fieldTypeVOPageApi = (queryForm: any) => {
	return service.post('/fieldType/voPage', queryForm)
}

//复杂列表
export const fieldTypeVOListApi = (queryForm: any) => {
	return service.post('/fieldType/voList', queryForm)
}

export const fieldTypeListApi = () => {
	return service.get('/fieldType/distinctList')
}
