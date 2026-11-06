import { Enum } from 'enum-plus'

// el-tag 的 type 联合（与 Element Plus 保持一致），供枚举项的 type 字段标注，拼错颜色名编译期即报错
export type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

// el-tag 的 effect 联合（与 Element Plus 保持一致）：与 type 正交，5×3=15 种视觉组合
// 约定：同一枚举内重色的项用 effect 区分（首个保持默认 light，后续用 plain），不传即走 el-tag 默认值
export type TagEffect = 'light' | 'dark' | 'plain'

// 数据库类型枚举（语义色只有 5 档，7 个数据库无法一一分配：优先照顾品牌色，
// 重色项用 effect 区分：首个保持 light，第二个用 plain，做到 7 项 7 样）
export const DB_TYPES = Enum({
	MySQL: { value: 'MySQL', label: 'MySQL', type: 'primary' as TagType },
	Oracle: { value: 'Oracle', label: 'Oracle', type: 'danger' as TagType },
	PostgreSQL: { value: 'PostgreSQL', label: 'PostgreSQL', type: 'primary' as TagType, effect: 'plain' as TagEffect },
	SQLServer: { value: 'SQLServer', label: 'SQLServer', type: 'warning' as TagType },
	DM: { value: 'DM', label: '达梦8', type: 'success' as TagType },
	KingBase: { value: 'KingBase', label: '人大金仓', type: 'danger' as TagType, effect: 'plain' as TagEffect },
	Clickhouse: { value: 'Clickhouse', label: 'Clickhouse', type: 'info' as TagType }
} as const)

// 模板组类型枚举（试点：带 type 字段的枚举项由 dict-tag 渲染成对应颜色的 el-tag）
export const TEMPLATE_GROUP_TYPES = Enum({
	Project: { value: 0, label: '项目模板', type: 'primary' as TagType },
	Table: { value: 1, label: '表模板', type: 'success' as TagType },
	Enum: { value: 2, label: '枚举模板', type: 'info' as TagType }
} as const)

// 模板类型枚举
export const TEMPLATE_TYPES = Enum({
	Directory: { value: 0, label: '目录', type: 'primary' as TagType },
	File: { value: 1, label: '模板文件', type: 'success' as TagType },
	Binary: { value: 2, label: '二进制文件', type: 'info' as TagType }
} as const)

// 项目生成方式
export const PROJECT_GENERATE_TYPES = Enum({
	Zip: { value: 0, label: 'ZIP压缩包', type: 'primary' as TagType },
	Local: { value: 1, label: '服务器本地', type: 'success' as TagType }
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
export const TABLE_POPUP_TYPES = Enum({
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

export type LayoutSize = (typeof LAYOUT_SIZE_TYPES.values)[number]
