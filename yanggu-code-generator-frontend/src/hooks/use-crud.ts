import type { UseCrudOptions } from '@/types/hooks/use-crud'
import { useCrudTable } from './use-crud-table'
import { useCrudForm } from './use-crud-form'

/**
 * 统一 CRUD hook
 *
 * 顶层共享 primaryKey / subject，table / form 各自独立 reactive 配置，
 * hook 内部注入共享字段并自动关联 afterSubmit → 刷新表格。
 *
 * @example
 * const tableConfig = reactive({ dataListApi, deleteApi, initQueryForm, columns, searchSchema })
 * const formConfig = reactive({ formSchema, submitApi, detailApi, initFormData })
 * const { tableHook, formHook } = useCrud({
 *   primaryKey: 'id',
 *   subject: '字段类型',
 *   table: tableConfig,
 *   form: formConfig
 * })
 */
export const useCrud = <VO = any, Query = any, DTO = any>(opts: UseCrudOptions<VO, Query, DTO>) => {
	const { primaryKey, subject, tableConfig: tableOpts, formConfig: formOpts } = opts

	// 注入共享字段到表格 reactive 配置（useCrudTable 内部会填充其他默认值）
	;(tableOpts as any).primaryKey ??= primaryKey
	;(tableOpts as any).subject ??= subject
	const tableHook = useCrudTable<VO, Query>(tableOpts)

	// 注入共享字段到表单 reactive 配置 + 自动关联 afterSubmit → 刷新表格
	;(formOpts as any).primaryKey ??= primaryKey
	;(formOpts as any).subject ??= subject
	const formHook = useCrudForm<DTO, VO>({
		...formOpts,
		afterSubmit: formOpts.afterSubmit ?? (() => tableHook.query.getDataList())
	})

	return { tableHook, formHook }
}
