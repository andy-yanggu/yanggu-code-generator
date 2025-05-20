import { EnumItem } from '@/utils/enum'

//数据库类型枚举
export const DB_TYPES: EnumItem[] = [
	{ label: 'MySQL', value: 'MySQL' },
	{ label: 'Oracle', value: 'Oracle' },
	{ label: 'PostgreSQL', value: 'PostgreSQL' },
	{ label: 'SQLServer', value: 'SQLServer' },
	{ label: '达梦8', value: 'DM' },
	{ label: '人大金仓', value: 'KingBase' },
	{ label: 'Clickhouse', value: 'Clickhouse' }
]

//模板组类型枚举
export const TEMPLATE_GROUP_TYPES: EnumItem[] = [
	{ label: '项目模板', value: 0 },
	{ label: '表模板', value: 1 },
	{ label: '枚举模板', value: 2 }
]

//模板类型枚举
export const TEMPLATE_TYPES: EnumItem[] = [
	{ label: '文件', value: 0 },
	{ label: '目录', value: 1 }
]

//项目生成方式
export const PROJECT_GENERATE_TYPES: EnumItem[] = [
	{ label: 'zip压缩包', value: 0 },
	{ label: '服务器本地', value: 1 }
]

//表单布局枚举
export const FORM_LAYOUT_TYPES: EnumItem[] = [
	{ label: '一列', value: 1 },
	{ label: '两列', value: 2 }
]
