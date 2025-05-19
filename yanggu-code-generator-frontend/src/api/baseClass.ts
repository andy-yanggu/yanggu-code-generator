import service from '@/utils/request'

//提交表单
export const baseClassSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/baseClass/update', dataForm)
	} else {
		//新增接口
		return service.post('/baseClass/add', dataForm)
	}
}

//删除接口
export const baseClassDeleteApi = (id: number) => {
	return service.delete('/baseClass/delete?id=' + id)
}

//批量删除
export const baseClassDeleteListApi = (idList: Array<number>) => {
	return service.delete('/baseClass/deleteList', { data: idList })
}

//查询详情
export const baseClassDetailApi = (id: number) => {
	return service.get('/baseClass/detail?id=' + id)
}

//批量查询
export const baseClassDetailListApi = (idList: Array<number>) => {
	return service.post('/baseClass/detailList', idList)
}

//简单分页
export const baseClassEntityPageApi = (queryForm: any) => {
	return service.post('/baseClass/entityPage', queryForm)
}

//简单列表
export const baseClassEntityListApi = (queryForm: any) => {
	return service.post('/baseClass/entityList', queryForm)
}

//复杂分页
export const baseClassVOPageApi = (queryForm: any) => {
	return service.post('/baseClass/voPage', queryForm)
}

//复杂列表
export const baseClassVOListApi = (queryForm: any) => {
	return service.post('/baseClass/voList', queryForm)
}
