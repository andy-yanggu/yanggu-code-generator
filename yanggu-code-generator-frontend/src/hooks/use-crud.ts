import type { UseCrudOptions } from '@/types/hooks/use-crud'
import type { CrudField, FormFieldSchema, SearchFieldSchema, TableColumnSchema } from '@/types/schema'
import { defaultsDeep } from '@/utils/tool'
import { useCrudTable } from './use-crud-table'
import { useCrudForm } from './use-crud-form'

// ======== 内置固定列 ========
const BUILTIN_SELECTION: TableColumnSchema = {
	key: 'selection',
	type: 'selection',
	label: '',
	width: 50,
	align: 'center',
	fixed: 'left',
	disabled: true
}
const BUILTIN_INDEX: TableColumnSchema = { key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true }
const BUILTIN_OPERATION: TableColumnSchema = { key: 'operation', label: '操作', fixed: 'right', width: 150, align: 'center', disabled: true }
const BUILTIN_CREATE_TIME: TableColumnSchema = {
	key: 'createTime',
	label: '创建时间',
	minWidth: 120,
	sortable: 'custom',
	align: 'center',
	showOverflowTooltip: true
}
const BUILTIN_UPDATE_TIME: TableColumnSchema = {
	key: 'updateTime',
	label: '修改时间',
	minWidth: 120,
	sortable: 'custom',
	align: 'center',
	showOverflowTooltip: true
}

// ======== 表单字段默认值 ========
const defaultFormFieldOptions = (): Partial<FormFieldSchema> => ({
	filterable: true,
	clearable: true
})

// ======== 查询字段默认值 ========
const defaultSearchFieldOptions = (): Partial<SearchFieldSchema> => ({
	filterable: true,
	clearable: true
})

// ======== 表格列默认值 ========
const defaultTableColumnOptions = (): Partial<TableColumnSchema> => ({
	align: 'center',
	showOverflowTooltip: true
})

// ======== 派生函数 ========
const buildFormSchema = (fields: CrudField[]): FormFieldSchema[] => {
	return fields
		.filter(f => f.inForm)
		.map(f => {
			// 顶层共享属性注入 form 子对象
			const shared: Partial<FormFieldSchema> = {}
			if (f.component !== undefined) shared.component = f.component
			if (f.options !== undefined) shared.options = f.options
			return defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...shared, ...f.form }, defaultFormFieldOptions())
		})
}

const buildSearchSchema = (fields: CrudField[]): SearchFieldSchema[] => {
	return fields
		.filter(f => f.inSearch)
		.map(f => {
			// 顶层共享属性注入 search 子对象
			const shared: Partial<SearchFieldSchema> = {}
			if (f.component !== undefined) shared.component = f.component
			if (f.options !== undefined) shared.options = f.options
			return defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...shared, ...f.search }, defaultSearchFieldOptions())
		})
}

const buildTableColumns = (fields: CrudField[], opts?: UseCrudOptions['tableConfig']): TableColumnSchema[] => {
	const cols: TableColumnSchema[] = []
	if (opts?.showSelection !== false) cols.push(BUILTIN_SELECTION)
	if (opts?.showIndex !== false) cols.push(BUILTIN_INDEX)
	cols.push(
		...fields
			.filter(f => f.inTable)
			.map(f => defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...f.table }, defaultTableColumnOptions()))
	)
	if (opts?.showCreateTime !== false) cols.push(BUILTIN_CREATE_TIME)
	if (opts?.showUpdateTime !== false) cols.push(BUILTIN_UPDATE_TIME)
	if (opts?.showOperation !== false) cols.push(BUILTIN_OPERATION)
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
export const useCrud = <VO = any, Query = any, DTO = any>(opts: UseCrudOptions<VO, Query, DTO>) => {
	// ====== 默认值 ======
	const defaultOptions = (): Partial<UseCrudOptions<VO, Query, DTO>> => ({
		primaryKey: 'id',
		subject: '数据'
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
