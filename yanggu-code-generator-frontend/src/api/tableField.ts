import service from '@/utils/request'

//提交表单
export const tableFieldSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/tableField/update', dataForm)
	} else {
		//新增接口
		return service.post('/tableField/add', dataForm)
	}
}

//删除接口
export const tableFieldDeleteApi = (id: number) => {
	return service.delete('/tableField/delete?id=' + id)
}

//批量删除
export const tableFieldDeleteListApi = (idList: Array<number>) => {
	return service.delete('/tableField/deleteList', { data: idList })
}

//查询详情
export const tableFieldDetailApi = (id: number) => {
	return service.get('/tableField/detail?id=' + id)
}

//批量查询
export const tableFieldDetailListApi = (idList: Array<number>) => {
	return service.post('/tableField/detailList', idList)
}

//简单分页
export const tableFieldEntityPageApi = (queryForm: any) => {
	return service.post('/tableField/entityPage', queryForm)
}

//简单列表
export const tableFieldEntityListApi = (queryForm: any) => {
	return service.post('/tableField/entityList', queryForm)
}

//复杂分页
export const tableFieldVOPageApi = (queryForm: any) => {
	return service.post('/tableField/voPage', queryForm)
}

//复杂列表
export const tableFieldVOListApi = (queryForm: any) => {
	return service.post('/tableField/voList', queryForm)
}
