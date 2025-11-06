import { service } from '@/utils/request'
import { createCrudApi, EnabledCrudApi } from '@/api/common'
import { PageQuery } from '@/api/common/type'

// 项目Entity
export interface GenProjectEntity {
	// ID主键自增
	id: number | string
	// 项目名
	projectName: string
	// 项目包名
	projectPackage: string
	// 项目版本
	projectVersion: string
	// 数据源ID
	datasourceId: number | string
	// 项目模板组ID
	projectTemplateGroupId: number | string
	// 表模板组ID
	tableTemplateGroupId: number | string
	// 枚举模板组ID
	enumTemplateGroupId: number | string
	// 后端路径
	backendPath: string
	// 前端路径
	frontendPath: string
	// 项目描述
	projectDesc: string
	// 作者
	author: string
	// Entity基类ID
	entityBaseClassId: number | string
	// VO基类ID
	voBaseClassId: number | string
	// 生成方式（0-zip压缩包，1-服务器本地）
	generatorType: number | string
	//
	projectTemplateGroupPropValue: object
	tableTemplateGroupPropValue: object
	enumTemplateGroupPropValue: object
}

// 项目Query查询参数
export interface GenProjectQuery extends PageQuery {
	// 项目名称
	projectName: string
	// 生成方式
	generatorType: number | string
}

// 特定api
interface CustomApi {
	// 项目下的表
	tableList: (queryForm: any) => Promise<any>
}

const baseUrl: string = '/gen/project'

// 项目API
export const genProjectApi: EnabledCrudApi<GenProjectEntity, GenProjectQuery> & CustomApi = {
	// 通用CRUD接口
	...createCrudApi(baseUrl),
	// 项目下的表
	tableList: (queryForm: any): Promise<any> => {
		return service.post(baseUrl + '/tableList', queryForm)
	}
}
