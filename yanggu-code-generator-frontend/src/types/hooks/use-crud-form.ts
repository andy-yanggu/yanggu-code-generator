import type { Key } from '@/types/common'
import type { FormItemRule } from 'element-plus'

/**
 * useCrudForm 配置选项
 */
export interface UseCrudFormOptions<DTO = any, VO = any> {
	/** 提交接口 */
	submitApi: (data: DTO) => Promise<any>
	/** 详情接口 */
	detailApi?: (id: Key) => Promise<VO>
	/** 表单数据初始值工厂 */
	initFormData: () => DTO
	/** 弹窗标题主体 */
	subject?: string
	/** 表单校验规则 */
	rules?: Record<string, FormItemRule[]>
	/** 表单标签宽度，默认 '100px' */
	labelWidth?: string
	/** 提交成功回调（默认关闭弹窗 + 刷新表格） */
	submitAfter?: (data: any) => void
}
