import type { Key } from '@/types/common'
import type { ComputedRef, MaybeRefOrGetter, Ref } from 'vue'
import type { FormItemRule } from 'element-plus'
import type { FormFieldSchema, FormMode, NormalizedFormField } from '@/types/schema'

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
	/** 表单数据初始值工厂（接收 formContext 作为参数） */
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
	/** 固定表单上下文（类似 queryContext），自动注入到 initFormData / beforeOpen。支持响应式（computed / ref / reactive） */
	formContext?: MaybeRefOrGetter<Record<string, any>>

	// ---- 生命周期回调 ----
	/** 弹窗打开前（数据重置前触发，接收 formContext） */
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

/**
 * useCrudForm 返回值契约
 *
 * 存在的理由同 UseCrudTableReturn：避开 `ReturnType<typeof useCrudForm>` 在泛型函数上
 * 只能用默认实参实例化、从而与页面侧具体实体类型比较时报 TS2719 的问题。
 *
 * 只带 DTO 一个类型形参：返回值对外暴露的数据只有 form.data（DTO 形态）。
 * VO 只在 options 侧参与 detailApi / beforeDataAssign / afterDataAssign，
 * 写进返回值契约会成为一个从未被使用的类型形参（eslint no-unused-vars 报错）。
 */
export interface UseCrudFormReturn<DTO = any> {
	/** 归一化后的表单字段（key/prop 已补全） */
	formSchema: NormalizedFormField[]
	dialog: {
		visible: boolean
		mode: FormMode
		title: string
		/** 详情接口加载中；true 时禁止提交，避免把未填充的空表单写回后端 */
		detailLoading: boolean
		open: (mode: FormMode, id?: Key) => void
		close: () => void
	}
	/**
	 * 表单状态。
	 *
	 * 注意：本组由 shallowReactive 包裹，**不会**解包内部的 ref / computed，
	 * 所以 rules 与 formRef 在契约里仍是 Ref 形态（模板中需写 formHook.form.rules.value）。
	 * 不能改成 reactive：formRef 一旦被解包，CrudDialog 的 `:ref="formHook.form.formRef"`
	 * 就会拿到组件实例而不是 Ref 对象，el-form 的校验/重置将全部失效。
	 */
	form: {
		data: DTO
		rules: ComputedRef<Record<string, FormItemRule[]>>
		labelWidth: string
		formRef: Ref<any>
	}
	submit: {
		execute: () => void
		loading: boolean
	}
}
