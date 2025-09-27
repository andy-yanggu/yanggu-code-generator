import { service } from '@/utils/request'

const baseUrl: string = '/${moduleNameCase}/${functionName}'

// 提交表单
export const ${functionName}SubmitApi = (dataForm: any) => {
if (dataForm.id) {
// 修改接口
return service.put(baseUrl + '/update', dataForm)
} else {
// 新增接口
return service.post(baseUrl + '/add', dataForm)
}
}

// 删除接口
export const ${functionName}DeleteApi = (id: number) => {
return service.delete(baseUrl + '/delete?id=' + id)
}

// 批量删除
export const ${functionName}DeleteListApi = (idList: number[]) => {
return service.delete(baseUrl + '/deleteList', { data: idList })
}

// 详情
export const ${functionName}DetailApi = (id: number) => {
return service.get(baseUrl + '/detail?id=' + id)
}

// 详情列表
export const ${functionName}DetailListApi = (idList: number[]) => {
return service.post(baseUrl + '/detailList', idList)
}

// 简单分页
export const ${functionName}EntityPageApi = (queryForm?: any) => {
return service.post(baseUrl + '/entityPage', queryForm ? queryForm : {})
}

// 简单列表
export const ${functionName}EntityListApi = (queryForm?: any) => {
return service.post(baseUrl + '/entityList', queryForm ? queryForm : {})
}

// 复杂分页
export const ${functionName}VOPageApi = (queryForm?: any) => {
return service.post(baseUrl + '/voPage', queryForm ? queryForm : {})
}

// 复杂列表
export const ${functionName}VOListApi = (queryForm?: any) => {
return service.post(baseUrl + '/voList', queryForm ? queryForm : {})
}
