import service from '@/utils/request'
import { createCrudApi } from '@/api/common'

const baseUrl: string = '/gen/enum'

export const genEnumApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 枚举批量生成代码检测
	generateCheck: (idList: number[]) => {
		return service.post(baseUrl + '/generateCheck', idList)
	}
}
