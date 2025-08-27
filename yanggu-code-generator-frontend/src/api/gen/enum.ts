import service from '@/utils/request'

//提交表单
export const enumSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/enum/update', dataForm)
	} else {
		//新增接口
		return service.post('/enum/add', dataForm)
	}
}

//删除接口
export const enumDeleteApi = (id: number) => {
	return service.delete('/enum/delete?id=' + id)
}

//批量删除
export const enumDeleteListApi = (idList: Array<number>) => {
	return service.delete('/enum/deleteList', { data: idList })
}

//查询详情
export const enumDetailApi = (id: number) => {
	return service.get('/enum/detail?id=' + id)
}

//批量查询
export const enumDetailListApi = (idList: Array<number>) => {
	return service.post('/enum/detailList', idList)
}

//简单分页
export const enumEntityPageApi = (queryForm: any) => {
	return service.post('/enum/entityPage', queryForm)
}

//简单列表
export const enumEntityListApi = (queryForm: any) => {
	return service.post('/enum/entityList', queryForm)
}

//复杂分页
export const enumVOPageApi = (queryForm: any) => {
	return service.post('/enum/voPage', queryForm)
}

//复杂列表
export const enumVOListApi = (queryForm: any) => {
	return service.post('/enum/voList', queryForm)
}

//枚举批量生成代码检测
export const enumGenerateCheckApi = (idList: Array<number>) => {
	return service.post('/enum/generateCheck', idList)
}
