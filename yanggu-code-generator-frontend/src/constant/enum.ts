import { EnumItem } from '@/types'

// 数据库类型枚举
export const DB_TYPES: EnumItem[] = [
	{ label: 'MySQL', value: 'MySQL' },
	{ label: 'Oracle', value: 'Oracle' },
	{ label: 'PostgreSQL', value: 'PostgreSQL' },
	{ label: 'SQLServer', value: 'SQLServer' },
	{ label: '达梦8', value: 'DM' },
	{ label: '人大金仓', value: 'KingBase' },
	{ label: 'Clickhouse', value: 'Clickhouse' }
]

// 模板组类型枚举
export const TEMPLATE_GROUP_TYPES: EnumItem[] = [
	{ label: '项目模板', value: 0 },
	{ label: '表模板', value: 1 },
	{ label: '枚举模板', value: 2 }
]

// 模板类型枚举
export const TEMPLATE_TYPES: EnumItem[] = [
	{ label: '目录', value: 0 },
	{ label: '模板文件', value: 1 },
	{ label: '二进制文件', value: 2 }
]

// 项目生成方式
export const PROJECT_GENERATE_TYPES: EnumItem[] = [
	{ label: 'ZIP压缩包', value: 0 },
	{ label: '服务器本地', value: 1 }
]

// 表单布局枚举
export const FORM_LAYOUT_TYPES: EnumItem[] = [
	{ label: '一列', value: 1 },
	{ label: '两列', value: 2 }
]

// 表生成功能枚举
export const TABLE_GENERATOR_FUNCTION_TYPES: EnumItem[] = [
	{ label: '新增', value: 0 },
	{ label: '修改', value: 1 },
	{ label: '删除', value: 2 },
	{ label: '详情', value: 3 },
	{ label: '分页', value: 4 },
	{ label: '列表', value: 5 },
	{ label: '导入', value: 6 },
	{ label: '导出', value: 7 }
]

// 表弹窗方式
export const TABLE_POPUP_TYPE_TYPES: EnumItem[] = [
	{ label: '对话框', value: 0 },
	{ label: '抽屉', value: 1 }
]

// 属性类型枚举
export const ATTR_TYPES: EnumItem[] = [
	{ label: 'Byte', value: 'Byte' },
	{ label: 'Short', value: 'Short' },
	{ label: 'Integer', value: 'Integer' },
	{ label: 'Long', value: 'Long' },
	{ label: 'BigInteger', value: 'BigInteger' },
	{ label: 'Float', value: 'Float' },
	{ label: 'Double', value: 'Double' },
	{ label: 'BigDecimal', value: 'BigDecimal' },
	{ label: 'Boolean', value: 'Boolean' },
	{ label: 'Character', value: 'Character' },
	{ label: 'String', value: 'String' },
	{ label: 'Date', value: 'Date' }
]

// 组件类型枚举
export const COMPONENT_TYPES: EnumItem[] = [
	{ label: '输入框', value: 0 },
	{ label: '数字输入框', value: 1 },
	{ label: '选择器', value: 2 },
	{ label: '单选框', value: 3 },
	{ label: '多选框', value: 4 },
	{ label: '开关', value: 5 }
]

// 字段布局方式枚举
export const COLUMN_SPAN_TYPES: EnumItem[] = [
	{ label: '1个字段', value: 1 },
	{ label: '2个字段', value: 2 }
]

// 生成产物类型枚举
export enum GeneratorProductTypeEnum {
	PROJECT = 0,
	TABLE = 1,
	ENUM = 2
}
