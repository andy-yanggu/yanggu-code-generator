import service from '@/utils/request'

// 所有可能的CRUD方法签名
interface CrudApi {
	// 新增接口
	add: (dataForm: any) => Promise<any>

	// 修改接口
	update: (dataForm: any) => Promise<any>

	// 提交表单接口
	submit: (dataForm: any) => Promise<any>

	// 单个删除接口
	delete: (id: number | string) => Promise<any>

	// 批量删除接口
	deleteList: (idList: number[] | string[]) => Promise<any>

	// 详情接口
	detail: (id: number | string) => Promise<any>

	// 批量详情接口
	detailList: (idList: number[] | string[]) => Promise<any>

	/**
	 * 分页查询基础实体数据
	 * - 只返回表的原始字段，不包含关联表信息
	 * - 用于简单分页展示和列表查询
	 */
	entityPage: (queryForm?: any) => Promise<any>

	/**
	 * 分页查询复杂视图数据（VO）
	 * - 可能包含关联表 JOIN 的额外信息
	 * - 用于需要显示完整业务信息的场景
	 */
	voPage: (queryForm?: any) => Promise<any>

	/**
	 * 列表查询基础实体数据（无分页）
	 * - 只返回表原始字段
	 */
	entityList: (queryForm?: any) => Promise<any>

	/**
	 * 列表查询复杂视图数据（VO）（无分页）
	 * - 包含关联表 JOIN 的额外信息
	 */
	voList: (queryForm?: any) => Promise<any>
}

// 配置接口，默认所有方法启用，禁用方法时显式设置false
export interface CrudConfig {
	add?: boolean
	update?: boolean
	submit?: boolean
	delete?: boolean
	deleteList?: boolean
	detail?: boolean
	detailList?: boolean
	entityPage?: boolean
	entityList?: boolean
	voPage?: boolean
	voList?: boolean
}

// ----------------------------------------------------
// 工具类型：根据 CrudConfig 过滤掉被显式设置为 false 的接口
// C：传入的配置类型（每个键为 CrudApi 的方法名，可选值为 false 或省略）
// CrudApi：完整的 CRUD API 方法集合
// ----------------------------------------------------
type EnabledCrudApi<C extends CrudConfig> = Pick<
	CrudApi,
	Exclude<
		keyof CrudApi,
		// 找出配置中显式为 false 的方法名，并排除
		{
			[K in keyof C]: C[K] extends false ? K : never
		}[keyof C]
	>
>

// ----------------------------------------------------
// 生成 CRUD API 工厂函数
// - baseUrl：接口基础路径
// - opts：可选配置，某些方法设为 false 表示禁用该方法
// 返回值：自动排除了禁用方法的 CRUD API 对象
// ----------------------------------------------------
export const createCrudApi = <C extends CrudConfig = CrudConfig>(baseUrl: string, opts?: C): EnabledCrudApi<C> => {
	const api = {} as any
	const defaultOpts = {
		add: false,
		update: false
	} as C
	const o = opts || defaultOpts

	const add = <K extends keyof CrudApi>(key: K, fn: CrudApi[K]) => {
		if (o[key] !== false) {
			api[key] = fn
		}
	}

	add('add', dataForm => service.post(`${baseUrl}/add`, dataForm))

	add('update', dataForm => service.put(`${baseUrl}/update`, dataForm))

	add('submit', dataForm => (dataForm.id ? service.put(`${baseUrl}/update`, dataForm) : service.post(`${baseUrl}/add`, dataForm)))

	add('delete', id => service.delete(`${baseUrl}/delete`, { params: { id } }))

	add('deleteList', idList => service.delete(`${baseUrl}/deleteList`, { data: idList }))

	add('detail', id => service.get(`${baseUrl}/detail`, { params: { id } }))

	add('detailList', idList => service.post(`${baseUrl}/detailList`, idList))

	add('entityPage', queryForm => service.post(`${baseUrl}/entityPage`, queryForm || {}))

	add('entityList', queryForm => service.post(`${baseUrl}/entityList`, queryForm || {}))

	add('voPage', queryForm => service.post(`${baseUrl}/voPage`, queryForm || {}))

	add('voList', queryForm => service.post(`${baseUrl}/voList`, queryForm || {}))

	return api
}
