import { downloadFile, service } from '@/utils/request'
import { Key, KeyArray } from '@/types/common'
import { PageQuery, PageVO } from '@/api/common/type'

/**
 * 通用 CRUD 接口定义
 * Entity: 数据表原始实体
 * Query: 查询参数（必须继承 PageQuery）
 * VO: 展示视图对象
 * DTO: 新增/修改请求体
 */
export interface CrudApi<Entity = any, Query extends PageQuery = PageQuery, VO = Entity, DTO = Entity> {
	// 新增接口
	add: (dataForm: DTO) => Promise<void>

	// 修改接口
	update: (dataForm: DTO & { id: Key }) => Promise<void>

	// 提交接口（根据是否存在 id 自动选择新增或更新）
	submit: (dataForm: DTO) => Promise<void>

	// 单个删除接口
	delete: (id: Key) => Promise<void>

	// 批量删除接口
	deleteList: (idList: KeyArray) => Promise<void>

	// 详情接口
	detail: (id: Key) => Promise<VO>

	// 批量详情接口
	detailList: (idList: KeyArray) => Promise<VO[]>

	/**
	 * 分页查询基础实体数据
	 * - 只返回表的原始字段，不包含关联表信息
	 * - 用于简单分页展示和列表查询
	 */
	entityPage: (queryForm?: Query) => Promise<PageVO<VO>>

	/**
	 * 分页查询复杂视图数据（VO）
	 * - 可能包含关联表 JOIN 的额外信息
	 * - 用于需要显示完整业务信息的场景
	 */
	voPage: (queryForm?: Query) => Promise<PageVO<VO>>

	/**
	 * 列表查询基础实体数据（无分页）
	 * - 只返回表原始字段
	 */
	entityList: (queryForm?: Query) => Promise<VO[]>

	/**
	 * 列表查询复杂视图数据（VO）（无分页）
	 * - 包含关联表 JOIN 的额外信息
	 */
	voList: (queryForm?: Query) => Promise<VO[]>

	/**
	 * 导出数据，勾选导出数据
	 */
	export: (idList: KeyArray) => Promise<void>

	/**
	 * 导入数据
	 */
	import: (formData: FormData) => Promise<void>
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
	export?: boolean
	import?: boolean
}

// 默认配置值（运行时使用）
const defaultCrudConfig: CrudConfig = {}

// ----------------------------------------------------
// 工具类型：根据 CrudConfig 过滤掉被显式设置为 false 的接口
// C：传入的配置类型（每个键为 CrudApi 的方法名，可选值为 false 或省略）
// CrudApi：完整的 CRUD API 方法集合
// ----------------------------------------------------
export type EnabledCrudApi<C extends CrudConfig, Entity, Query extends PageQuery, VO, DTO> = Pick<
	CrudApi,
	Exclude<
		keyof CrudApi<Entity, Query, VO, DTO>,
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
export const createCrudApi = <Entity = any, Query extends PageQuery = PageQuery, VO = Entity, DTO = Entity, C extends CrudConfig = CrudConfig>(
	baseUrl: string,
	opts?: C
): EnabledCrudApi<C, Entity, Query, VO, DTO> => {
	// api 对象，用于保存生成的方法
	const api = {} as Partial<EnabledCrudApi<C, Entity, Query, VO, DTO>>

	// 合并默认配置（保留传入项）
	const o = {
		...defaultCrudConfig,
		...opts
	} satisfies CrudConfig

	const add = <K extends keyof CrudApi>(key: K, fn: CrudApi[K]) => {
		if (o[key] !== false) {
			;(api[key as unknown as keyof EnabledCrudApi<C, Entity, Query, VO, DTO>] as CrudApi[K]) = fn
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

	add('export', idList => downloadFile(`${baseUrl}/export`, { idList }))

	add('import', formData => service.post(`${baseUrl}/import`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }))

	return api as EnabledCrudApi<C, Entity, Query, VO, DTO>
}
export class CrudApiWithExtra {}
