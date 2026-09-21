import { Enum } from 'enum-plus'

// 数据库类型枚举
export const DB_TYPES = Enum({
	MySQL: { value: 'MySQL', label: 'MySQL' },
	Oracle: { value: 'Oracle', label: 'Oracle' },
	PostgreSQL: { value: 'PostgreSQL', label: 'PostgreSQL' },
	SQLServer: { value: 'SQLServer', label: 'SQLServer' },
	DM: { value: 'DM', label: '达梦8' },
	KingBase: { value: 'KingBase', label: '人大金仓' },
	Clickhouse: { value: 'Clickhouse', label: 'Clickhouse' }
} as const)

// 模板组类型枚举
export const TEMPLATE_GROUP_TYPES = Enum({
	Project: { value: 0, label: '项目模板' },
	Table: { value: 1, label: '表模板' },
	Enum: { value: 2, label: '枚举模板' }
} as const)

// 模板类型枚举
export const TEMPLATE_TYPES = Enum({
	Directory: { value: 0, label: '目录' },
	File: { value: 1, label: '模板文件' },
	Binary: { value: 2, label: '二进制文件' }
} as const)

// 项目生成方式
export const PROJECT_GENERATE_TYPES = Enum({
	Zip: { value: 0, label: 'ZIP压缩包' },
	Local: { value: 1, label: '服务器本地' }
} as const)

// 表单布局枚举
export const FORM_LAYOUT_TYPES = Enum({
	OneColumn: { value: 1, label: '一列' },
	TwoColumns: { value: 2, label: '两列' }
} as const)

// 表生成功能枚举
export const TABLE_GENERATOR_FUNCTION_TYPES = Enum({
	Add: { value: 0, label: '新增' },
	Update: { value: 1, label: '修改' },
	Delete: { value: 2, label: '删除' },
	Detail: { value: 3, label: '详情' },
	Page: { value: 4, label: '分页' },
	List: { value: 5, label: '列表' },
	Import: { value: 6, label: '导入' },
	Export: { value: 7, label: '导出' }
} as const)

// 表弹窗方式
export const TABLE_POPUP_TYPE_TYPES = Enum({
	Dialog: { value: 0, label: '对话框' },
	Drawer: { value: 1, label: '抽屉' }
} as const)

// 属性类型枚举
export const ATTR_TYPES = Enum({
	Byte: { value: 'Byte', label: 'Byte' },
	Short: { value: 'Short', label: 'Short' },
	Integer: { value: 'Integer', label: 'Integer' },
	Long: { value: 'Long', label: 'Long' },
	BigInteger: { value: 'BigInteger', label: 'BigInteger' },
	Float: { value: 'Float', label: 'Float' },
	Double: { value: 'Double', label: 'Double' },
	BigDecimal: { value: 'BigDecimal', label: 'BigDecimal' },
	Boolean: { value: 'Boolean', label: 'Boolean' },
	Character: { value: 'Character', label: 'Character' },
	String: { value: 'String', label: 'String' },
	Date: { value: 'Date', label: 'Date' }
} as const)

// 组件类型枚举
export const COMPONENT_TYPES = Enum({
	Input: { value: 0, label: '输入框' },
	NumberInput: { value: 1, label: '数字输入框' },
	Select: { value: 2, label: '选择器' },
	Radio: { value: 3, label: '单选框' },
	Checkbox: { value: 4, label: '多选框' },
	Switch: { value: 5, label: '开关' }
} as const)

// 字段布局方式枚举
export const COLUMN_SPAN_TYPES = Enum({
	One: { value: 1, label: '1个字段' },
	Two: { value: 2, label: '2个字段' }
} as const)

// 生成产物类型枚举
export const GENERATOR_PRODUCT_TYPES = Enum({
	Project: { value: 0, label: '项目' },
	Table: { value: 1, label: '表' },
	Enum: { value: 2, label: '枚举' }
} as const)

// 布局大小枚举
export const LAYOUT_SIZE_TYPES = Enum({
	Large: { value: 'large', label: '较大' },
	Default: { value: 'default', label: '默认' },
	Small: { value: 'small', label: '稍小' }
} as const)

export type LayoutSize = typeof LAYOUT_SIZE_TYPES.values[number]
