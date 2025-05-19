import service from '@/utils/request'

//提交表单
export const enumItemSubmitApi = (dataForm: any) => {
	if (dataForm.id) {
		//修改接口
		return service.put('/enumItem/update', dataForm)
	} else {
		//新增接口
		return service.post('/enumItem/add', dataForm)
	}
}

//删除接口
export const enumItemDeleteApi = (id: number) => {
	return service.delete('/enumItem/delete?id=' + id)
}

//批量删除
export const enumItemDeleteListApi = (idList: Array<number>) => {
	return service.delete('/enumItem/deleteList', { data: idList })
}

//查询详情
export const enumItemDetailApi = (id: number) => {
	return service.get('/enumItem/detail?id=' + id)
}

//批量查询
export const enumItemDetailListApi = (idList: Array<number>) => {
	return service.post('/enumItem/detailList', idList)
}

//简单分页
export const enumItemEntityPageApi = (queryForm: any) => {
	return service.post('/enumItem/entityPage', queryForm)
}

//简单列表
export const enumItemEntityListApi = (queryForm: any) => {
	return service.post('/enumItem/entityList', queryForm)
}

//复杂分页
export const enumItemVOPageApi = (queryForm: any) => {
	return service.post('/enumItem/voPage', queryForm)
}

//复杂列表
export const enumItemVOListApi = (queryForm: any) => {
	return service.post('/enumItem/voList', queryForm)
}
