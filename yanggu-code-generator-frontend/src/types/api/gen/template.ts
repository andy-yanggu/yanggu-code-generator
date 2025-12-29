import { Key, PageQuery } from '@/types'

/**
 * 模板管理实体
 */
export interface GenTemplateEntity {
	// 主键ID
	id: Key
	// 模板组ID
	templateGroupId: number
	// 模板名称
	templateName: string
	// 父级ID
	parentId: number
	// 描述
	templateDesc: string
	// 文件或者目录名称
	fileName: string
	// 模板内容
	templateContent: string
	// 模板类型（0-目录，1-模板文件，2-二进制文件）
	templateType: number
	// 二进制原始文件名
	binaryOriginalFileName: string
	// 条件表达式
	conditionExpression: string
	// 模板路径
	templatePath?: string
}

/**
 * 模板管理查询参数
 */
export interface GenTemplateQuery extends PageQuery {
	// 模板组ID
	templateGroupId?: number | string
	// 模板名称
	templateName?: string
	// 模板类型
	templateType?: number | string
	// 模板组类型
	templateGroupType?: number | string
	// 模板组ID列表
	templateGroupIdList?: number[]
	// 模板组名称
	templateGroupName?: string
	// 文件名称
	fileName?: string
}
