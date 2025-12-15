import { Key } from '@/types'

// 表单模式：新增、修改、复制
export const FORM_TYPES: string[] = ['add', 'update', 'copy', 'detail'] as const
export type FormType = (typeof FORM_TYPES)[number]

export interface FormOptions<VO> {
	// 提交API
	submitApi: (data: VO) => Promise<any>
	// 详情API
	detailApi: (id: Key) => Promise<VO>
	// 初始化表单数据函数
	initFormData: (ctx?: Record<string, any>) => VO
	// 初始化之前调用
	initBefore?: () => void
	// 表单数据
	dataForm: VO & { id?: Key }
	// 初始化之后调用
	initAfter?: () => void
	// 提交之前操作
	submitBefore?: () => void
	// 触发事件
	emit?: any
	// 提示信息
	message?: string
	// 提示时长
	duration?: number
}
