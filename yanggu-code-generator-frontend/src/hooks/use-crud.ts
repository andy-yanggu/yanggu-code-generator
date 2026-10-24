import type { UseCrudOptions } from '@/types/hooks/use-crud'
import type { UseCrudFormReturn } from '@/types/hooks/use-crud-form'
import type { UseCrudTableReturn } from '@/types/hooks/use-crud-table'
import type { CrudField, FormFieldSchema, SearchFieldSchema, TableColumnSchema } from '@/types/schema'
import {
	DEFAULT_FORM_FIELD_OPTIONS,
	DEFAULT_PRIMARY_KEY,
	DEFAULT_SEARCH_FIELD_OPTIONS,
	DEFAULT_SUBJECT,
	DEFAULT_TABLE_COLUMN_OPTIONS
} from '@/config/crud'
import { defaultsDeep } from '@/utils/tool'
import {
	createBuiltinCreateTime,
	createBuiltinIndex,
	createBuiltinOperation,
	createBuiltinSelection,
	createBuiltinUpdateTime,
	useCrudTable
} from './use-crud-table'
import { useCrudForm } from './use-crud-form' // ======== 派生函数 ========

// ======== 派生函数 ========

/**
 * 拍平字段树：分组节点（含 children）本身不进表单/查询，用其叶子字段替代。
 * 当前仅 2 层，递归实现以防未来放宽层级。
 */
const flattenLeafFields = (fields: CrudField[]): CrudField[] => {
	const result: CrudField[] = []
	for (const f of fields) {
		if (f.children?.length) {
			result.push(...flattenLeafFields(f.children))
		} else {
			result.push(f)
		}
	}
	return result
}

const buildFormSchema = (fields: CrudField[]): FormFieldSchema[] => {
	return flattenLeafFields(fields)
		.filter(f => f.inForm)
		.map(f => {
			// 顶层共享属性注入 form 子对象（f.form 里的同名属性优先级更高）
			const shared: Partial<FormFieldSchema> = {}
			if (f.component !== undefined) shared.component = f.component
			if (f.options !== undefined) shared.options = f.options
			if (f.filterable !== undefined) shared.filterable = f.filterable
			if (f.clearable !== undefined) shared.clearable = f.clearable
			if (f.formTableTooltip !== undefined) shared.tooltip = f.formTableTooltip
			return defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...shared, ...f.form }, DEFAULT_FORM_FIELD_OPTIONS)
		})
}

const buildSearchSchema = (fields: CrudField[]): SearchFieldSchema[] => {
	return flattenLeafFields(fields)
		.filter(f => f.inSearch)
		.map(f => {
			// 顶层共享属性注入 search 子对象（f.search 里的同名属性优先级更高）
			const shared: Partial<SearchFieldSchema> = {}
			if (f.component !== undefined) shared.component = f.component
			if (f.options !== undefined) shared.options = f.options
			if (f.filterable !== undefined) shared.filterable = f.filterable
			if (f.clearable !== undefined) shared.clearable = f.clearable
			return defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...shared, ...f.search }, DEFAULT_SEARCH_FIELD_OPTIONS)
		})
}

/** 由单个 CrudField 构建表格列；分组节点（含 children）递归构建子列，且自身不带 prop */
const buildColumn = (f: CrudField): TableColumnSchema => {
	const base: Partial<TableColumnSchema> = {}
	if (f.formTableTooltip !== undefined) base.tooltip = f.formTableTooltip
	if (f.children?.length) {
		const children = f.children.filter(c => c.inTable).map(buildColumn)
		return defaultsDeep({ key: f.key, label: f.label, ...base, ...f.table, children }, DEFAULT_TABLE_COLUMN_OPTIONS)
	}
	return defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...base, ...f.table }, DEFAULT_TABLE_COLUMN_OPTIONS)
}

const buildTableColumns = (fields: CrudField[], opts?: UseCrudOptions['tableConfig']): TableColumnSchema[] => {
	const cols: TableColumnSchema[] = []
	if (opts?.showSelection !== false) cols.push(createBuiltinSelection())
	if (opts?.showIndex !== false) cols.push(createBuiltinIndex())
	cols.push(...fields.filter(f => f.inTable).map(buildColumn))
	if (opts?.showCreateTime !== false) cols.push(createBuiltinCreateTime())
	if (opts?.showUpdateTime !== false) cols.push(createBuiltinUpdateTime())
	if (opts?.showOperation !== false) cols.push(createBuiltinOperation())
	return cols
}

/**
 * 统一 CRUD hook
 *
 * 顶层共享 primaryKey / subject，table / form 各自独立 reactive 配置，
 * hook 内部注入共享字段并自动关联 afterSubmit → 刷新表格。
 *
 * 支持两种模式：
 * 1. fields 模式：提供 fields 数组，自动派生 formSchema / searchSchema / tableColumns
 * 2. 传统模式：直接传 formSchema / searchSchema / tableColumns（向后兼容）
 */
export const useCrud = <VO = any, Query = any, DTO = any>(
	opts: UseCrudOptions<VO, Query, DTO>
): { tableHook: UseCrudTableReturn<VO, Query>; formHook: UseCrudFormReturn<DTO> } => {
	// ====== 默认值 ======
	const defaultOptions = (): Partial<UseCrudOptions<VO, Query, DTO>> => ({
		primaryKey: DEFAULT_PRIMARY_KEY,
		subject: DEFAULT_SUBJECT
	})

	defaultsDeep(opts, defaultOptions())

	const { primaryKey, subject, fields, tableConfig: tableOpts, formConfig: formOpts } = opts

	// 如果提供了 fields，自动派生并注入到子配置
	if (fields) {
		formOpts.formSchema ??= buildFormSchema(fields)
		tableOpts.searchSchema ??= buildSearchSchema(fields)
		tableOpts.tableColumns ??= buildTableColumns(fields, tableOpts)
	}

	// 注入共享字段到子配置（顶层默认值向下透传）
	tableOpts.primaryKey ??= primaryKey
	tableOpts.subject ??= subject
	const tableHook = useCrudTable<VO, Query>(tableOpts)

	formOpts.primaryKey ??= primaryKey
	formOpts.subject ??= subject
	const formHook = useCrudForm<DTO, VO>({
		...formOpts,
		afterSubmit: formOpts.afterSubmit ?? (() => tableHook.query.getDataList())
	})

	return { tableHook, formHook }
}
