import service from '@/utils/request'

//表预览代码
export const generatorTablePreviewApi = (dataForm: any) => {
	return service.post('/generator/table/preview', dataForm)
}

//
export const generatorTableTreeDataApi = (dataForm: any) => {
	return service.post('/generator/table/treeData', dataForm)
}
