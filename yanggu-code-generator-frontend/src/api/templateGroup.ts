import service from '@/utils/request'

//提交表单
export const templateGroupSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/templateGroup/update', dataForm)
	} else {
		//新增接口
		return service.post('/templateGroup/add', dataForm)
	}
}

//删除接口
export const templateGroupDeleteApi = (id: number) => {
	return service.delete('/templateGroup/delete?id=' + id)
}

//批量删除
export const templateGroupDeleteListApi = (idList: Array<number>) => {
	return service.delete('/templateGroup/deleteList', { data: idList })
}

//查询详情
export const templateGroupDetailApi = (id: number) => {
	return service.get('/templateGroup/detail?id=' + id)
}

//批量查询
export const templateGroupDetailListApi = (idList: Array<number>) => {
	return service.post('/templateGroup/detailList', idList)
}

//简单分页
export const templateGroupEntityPageApi = (queryForm: any) => {
	return service.post('/templateGroup/entityPage', queryForm)
}

//简单列表
export const templateGroupEntityListApi = (queryForm: any) => {
	return service.post('/templateGroup/entityList', queryForm)
}

//复杂分页
export const templateGroupVOPageApi = (queryForm: any) => {
	return service.post('/templateGroup/voPage', queryForm)
}

//复杂列表
export const templateGroupVOListApi = (queryForm: any) => {
	return service.post('/templateGroup/voList', queryForm)
}

//复制模板组
export const copyTemplateApi = (dataForm: any) => {
	return service.post('/templateGroup/copy', dataForm)
}

//导出模板组
export const exportTemplateGroupApi = (tableIdList: any) => {
	const listString = tableIdList.join(',')
	location.href = import.meta.env.VITE_API_URL + '/templateGroup/export?idList=' + listString
}
