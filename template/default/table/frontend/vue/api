import service from '@/utils/request'

//提交表单
export const ${functionName}SubmitApi = (dataForm: any) => {
    if (dataForm.id) {
        //修改接口
        return service.put('/${functionName}/update', dataForm)
    } else {
        //新增接口
        return service.post('/${functionName}/add', dataForm)
    }
}

//删除接口
export const ${functionName}DeleteApi = (id: number) => {
    return service.delete('/${functionName}/delete?id=' + id)
}

//批量删除
export const ${functionName}DeleteListApi = (idList: Array<number>) => {
    return service.delete('/${functionName}/deleteList', { data: idList })
}

//查询详情
export const ${functionName}DetailApi = (id: number) => {
    return service.get('/${functionName}/detail?id=' + id)
}

//批量查询
export const ${functionName}DetailListApi = (idList: Array<number>) => {
    return service.post('/${functionName}/detailList', idList)
}

//简单分页
export const ${functionName}EntityPageApi = (queryForm: any) => {
    return service.post('/${functionName}/entityPage', queryForm)
}

//简单列表
export const ${functionName}EntityListApi = (queryForm: any) => {
    return service.post('/${functionName}/entityList', queryForm)
}

//复杂分页
export const ${functionName}VOPageApi = (queryForm: any) => {
    return service.post('/${functionName}/voPage', queryForm)
}

//复杂列表
export const ${functionName}VOListApi = (queryForm: any) => {
    return service.post('/${functionName}/voList', queryForm)
}