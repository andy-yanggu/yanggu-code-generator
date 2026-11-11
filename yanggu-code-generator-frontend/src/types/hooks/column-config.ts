/**
 * 列配置 —— 定义表格列的元数据
 *
 * key 同时作为：唯一标识、命名插槽名、store 持久化 key
 *
 * 使用方式：
 * ```ts
 * const columns: ColumnConfig[] = [
 *   { key: 'selection', type: 'selection', width: 50, fixed: 'left', disabled: true },
 *   { key: 'name', prop: 'name', label: '名称', minWidth: 120 },
 *   { key: 'operation', label: '操作', fixed: 'right', width: 150, disabled: true },
 * ]
 * ```
 */
export interface ColumnConfig {
	/** 唯一标识，同时作为命名插槽名 */
	key: string
	/** 列标题 */
	label: string
	/** 列属性字段名（selection/index 等特殊列无 prop） */
	prop?: string
	/** 列类型 */
	type?: 'selection' | 'index'

	// ---- ElTableColumn 透传属性 ----
	/** 列宽度 */
	width?: number | string
	/** 列最小宽度 */
	minWidth?: number
	/** 固定列 */
	fixed?: 'left' | 'right' | boolean
	/** 列内容对齐方式 */
	align?: 'left' | 'center' | 'right'
	/** 列头对齐方式 */
	headerAlign?: 'left' | 'center' | 'right'
	/** 是否可排序 */
	sortable?: boolean | 'custom'
	/** 是否超出时显示 tooltip */
	showOverflowTooltip?: boolean
	/** 格式化单元格内容（与 ElTableColumn formatter 签名一致） */
	formatter?: (row: any, column?: any, cellValue?: any) => any

	// ---- 列设置控制 ----
	/** 是否显示，默认 true */
	visible?: boolean
	/** 是否在列设置面板中禁用（不可隐藏/拖动），固定列建议设为 true */
	disabled?: boolean
}
