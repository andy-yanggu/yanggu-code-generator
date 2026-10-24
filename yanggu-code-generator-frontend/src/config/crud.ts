import type { FormFieldSchema, SearchFieldSchema, TableColumnSchema } from '@/types/schema'

// ======== CRUD 组件体系共享默认值 ========
// 收拢各 hook / 组件原先散落的同义默认值：此前分页档位硬编码了 3 处、
// 主键名 'id' 5 处、主题名 '数据' 4 处，改一处漏一处就会造成
// 「hook 与组件各用一套默认值」的不一致。统一从这里引用。
// 命名遵循项目配置常量规范（大写下划线 + as const）。
// 数组例外：加 as const 会变成 readonly 数组，无法赋给 number[] 契约属性，
// 因此显式标注类型，消费侧用展开拷贝隔离引用。

// 通用
export const DEFAULT_PRIMARY_KEY = 'id'
export const DEFAULT_SUBJECT = '数据'
export const DEFAULT_FORM_LABEL_WIDTH = '100px'

// 表格
export const DEFAULT_TABLE_MAX_HEIGHT = '60vh'
export const DEFAULT_TABLE_COLUMN_OPTIONS = { align: 'center', showOverflowTooltip: true } as const satisfies Partial<TableColumnSchema>

// 分页
export const DEFAULT_PAGE_SIZES: number[] = [10, 20, 50, 100, 200]
export const DEFAULT_PAGE_SIZE = 10

// 表单 / 查询字段共享默认值：生产端（useCrud 注入 schema）与消费端（DynamicField 兜底）共用同一真源
export const DEFAULT_FIELD_CLEARABLE = true
export const DEFAULT_FIELD_FILTERABLE = true
export const DEFAULT_FORM_FIELD_OPTIONS = { filterable: DEFAULT_FIELD_FILTERABLE, clearable: DEFAULT_FIELD_CLEARABLE } as const satisfies Partial<FormFieldSchema>
export const DEFAULT_SEARCH_FIELD_OPTIONS = { filterable: DEFAULT_FIELD_FILTERABLE, clearable: DEFAULT_FIELD_CLEARABLE } as const satisfies Partial<SearchFieldSchema>

// 导入导出提示文案
export const DEFAULT_EXPORT_SUCCESS_MESSAGE = '导出成功，请查看下载的文件'
export const DEFAULT_IMPORT_SUCCESS_MESSAGE = '导入成功，请查看数据'
