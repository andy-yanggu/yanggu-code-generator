import service from '@/utils/request'

//表预览代码
export const generatorTablePreviewApi = (dataForm: any) => {
	return service.post('/generator/table/preview', dataForm)
}

//表树形数据
export const generatorTableTreeDataApi = (dataForm: any) => {
	return service.post('/generator/table/treeData', dataForm)
}

//下载单文件
export const generatorTableDownloadApi = (dataForm: any) => {
	location.href =
		import.meta.env.VITE_API_URL + '/generator/table/download-template-content?tableId=' + dataForm.tableId + '&templateId=' + dataForm.templateId
}

// 生成代码（zip压缩包）
export const generatorTableDownloadZipApi = (dataForm: any) => {
	location.href =
		import.meta.env.VITE_API_URL + '/generator/table/downloadZip?tableId=' + dataForm.tableId + '&templateIdList=' + dataForm.templateIdList.join(',')
}

// 下载到本地
export const generatorTableDownloadLocalApi = (dataForm: any) => {
	return service.post('/generator/table/downloadLocal', dataForm)
}
