import type { UseCrudTableOptions } from './use-crud-table'
import type { UseCrudFormOptions } from './use-crud-form'
import type { CrudField } from '@/types/schema'

/**
 * useCrud 统一配置选项
 *
 * 顶层放共享字段（primaryKey / subject），
 * table / form 各自使用独立的配置对象，互不干扰。
 */
export interface UseCrudOptions<VO = any, Query = any, DTO = any> {
	/** 主键字段名，默认 'id' */
	primaryKey?: string
	/** 主体名称，默认 '数据'。用于删除/导出提示文案和弹窗标题 */
	subject?: string
	/** 统一字段声明（提供后自动派生 formSchema / searchSchema / tableColumns） */
	fields?: CrudField[]
	/** 表格配置 */
	tableConfig: UseCrudTableOptions<VO, Query>
	/** 表单配置 */
	formConfig: UseCrudFormOptions<DTO, VO>
}
