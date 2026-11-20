// 代码生成统一查询相关类型

// 项目生成的子产物过滤（仅PROJECT）
export interface ProjectFilter {
	tableIds?: number[]
	enumIds?: number[]
	tableTemplateIds?: number[]
	enumTemplateIds?: number[]
}

// 代码生成统一查询
export interface GeneratorQuery {
	// 产物类型：0项目 1表 2枚举
	productType: number
	// 产物ID列表：项目=[projectId]，表=tableIds，枚举=enumIds
	productIds: number[]
	// 模板ID列表，空=全量；PROJECT 时指项目级模板
	templateIds?: number[]
	// 项目子产物过滤，仅 productType=0 时允许携带
	projectFilter?: ProjectFilter
}

// 项目向导专用：projectFilter 必填约束
export interface ProjectGeneratorQuery extends GeneratorQuery {
	productType: 0
	projectFilter: ProjectFilter
}

// 模板测试查询
export interface TemplateTestQuery {
	projectId: number | string
	templateGroupId: number
	templateGroupType: number
	templateId: number
	templateContent: string
	testId: number
}