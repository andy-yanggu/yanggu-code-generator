import service from '@/utils/request'

//提交表单
export const templateSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/template/update', dataForm)
	} else {
		//新增接口
		return service.post('/template/add', dataForm)
	}
}

//删除接口
export const templateDeleteApi = (id: number) => {
	return service.delete('/template/delete?id=' + id)
}

//批量删除
export const templateDeleteListApi = (idList: Array<number>) => {
	return service.delete('/template/deleteList', { data: idList })
}

//查询详情
export const templateDetailApi = (id: number) => {
	return service.get('/template/detail?id=' + id)
}

//批量查询
export const templateDetailListApi = (idList: Array<number>) => {
	return service.post('/template/detailList', idList)
}

//简单分页
export const templateEntityPageApi = (queryForm: any) => {
	return service.post('/template/entityPage', queryForm)
}

//简单列表
export const templateEntityListApi = (queryForm: any) => {
	return service.post('/template/entityList', queryForm)
}

//复杂分页
export const templateVOPageApi = (queryForm: any) => {
	return service.post('/template/voPage', queryForm)
}

//复杂列表
export const templateVOListApi = (queryForm: any) => {
	return service.post('/template/voList', queryForm)
}

//树形数据
export const templateTreeDataApi = (templateGroupId: number) => {
	return service.get('/template/tree?templateGroupId=' + templateGroupId)
}

//修改模板内容
export const templateUpdateContentApi = (dataForm: any) => {
	return service.put('/template/updateContent', dataForm)
}
