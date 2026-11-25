import { Key } from '@/types'

// 表单模式：新增、修改、复制
export const FORM_TYPES = ['add', 'update', 'copy', 'detail'] as const
export type FormType = (typeof FORM_TYPES)[number]

// 表单初始化参数
export interface FormInitOptions {
	// 表单模式
	type: FormType // add | update | detail | copy

	// 业务主键
	id?: Key

	// 初始化上下文
	ctx?: Record<string, any>
}
