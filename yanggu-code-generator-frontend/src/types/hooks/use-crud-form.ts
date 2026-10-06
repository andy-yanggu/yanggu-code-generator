import type { Key } from '@/types/common'
import type { FormItemRule } from 'element-plus'
import type { FormFieldSchema } from '@/types/schema'

/**
 * useCrudForm 配置选项
 */
export interface UseCrudFormOptions<DTO = any, VO = any> {
	// ---- 表单字段 ----
	/** 表单字段配置 */
	formSchema?: FormFieldSchema[]

	// ---- 接口 ----
	/** 提交接口 */
	submitApi: (data: DTO) => Promise<any>
	/** 详情接口 */
	detailApi?: (id: Key) => Promise<VO>
	/** 表单数据初始值工厂（可接收上下文参数） */
	initFormData: (ctx?: any) => DTO

	// ---- 基础配置 ----
	/** 主键字段名，默认 'id'。copy 模式下用于清除主键 */
	primaryKey?: string
	/** 弹窗标题主体，如「字段类型」 */
	subject?: string
	/** 完全自定义弹窗标题（优先级最高） */
	title?: string
	/** 表单校验规则 */
	rules?: Record<string, FormItemRule[]>
	/** 表单标签宽度，默认 '100px' */
	labelWidth?: string

	// ---- 生命周期回调 ----
	/** 弹窗打开前（数据重置前触发，可接收上下文） */
	beforeOpen?: (ctx?: any) => void
	/** 弹窗打开后（数据重置完成触发） */
	afterOpen?: () => void
	/** 详情数据赋值前（可预处理接口返回数据） */
	beforeDataAssign?: (data: VO) => void
	/** 详情数据赋值后（可进行二次处理） */
	afterDataAssign?: (data: VO) => void
	/** 提交前（校验通过后、调接口之前） */
	beforeSubmit?: () => void
	/** 提交成功后（默认关闭弹窗 + 刷新表格） */
	afterSubmit?: (data: any) => void

	// ---- 错误处理 ----
	/** 详情接口失败回调 */
	onDetailError?: (error: any) => void
	/** 提交接口失败回调 */
	onSubmitError?: (error: any) => void

	// ---- 消息 ----
	/** 自定义成功消息（字符串或按模式返回字符串的函数） */
	successMessage?: string | ((mode: string) => string)
}
