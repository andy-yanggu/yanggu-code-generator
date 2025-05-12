//数据库类型枚举
export const DB_TYPES = [
	{ label: 'MySQL', value: 'MySQL' },
	{ label: 'Oracle', value: 'Oracle' },
	{ label: 'PostgreSQL', value: 'PostgreSQL' },
	{ label: 'SQLServer', value: 'SQLServer' },
	{ label: '达梦8', value: 'DM' },
	{ label: '人大金仓', value: 'KingBase' },
	{ label: 'Clickhouse', value: 'Clickhouse' }
]

//模板组类型枚举
export const TEMPLATE_GROUP_TYPES = [
	{ label: '项目', value: 0 },
	{ label: '表', value: 1 }
]

//模板类型枚举
export const TEMPLATE_TYPES = [
	{ label: '文件', value: 0 },
	{ label: '目录', value: 1 }
]
