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

/**
 * 模板变量分类
 */
export type VariableKind = 'SCALAR' | 'SCALAR_LIST' | 'MODEL_LIST' | 'OBJECT' | 'MAP'

/**
 * 模板变量目录节点
 */
export interface VariableNode {
	// 属性名，MAP 节点的子项为模板组属性 propKey
	name: string
	// 从根算起的完整属性路径
	path: string
	// Java 类型简名
	javaType: string
	// 变量分类
	kind: VariableKind
	// 中文说明，字段缺 @VarDesc 时为空串
	desc: string
	// 默认循环变量名，集合类有值
	loopVar: string
	// 渲染时是否可能为 null
	nullable: boolean
	// 渲染时的实际值文本（敏感值已掩码，集合类为「共 N 条」）；仅测试页带值目录填充，结构目录为 undefined
	value?: string
	// 子变量列表
	children: VariableNode[]
}
