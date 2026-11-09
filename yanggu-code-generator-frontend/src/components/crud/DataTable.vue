<template>
	<el-card ref="cardRef" class="layout-table-card" :class="{ 'is-maximized': maximized }" shadow="hover">
		<!-- 工具栏整块可由 showTableHeader 关闭：v-if 在 slot template 上是条件插槽，
		     false 时 el-card 的 $slots.header 为空、不渲染 header 容器，不会残留空白条 -->
		<template v-if="showTableHeader" #header>
			<div class="table-tool-bar">
				<div v-if="showToolbarLeft" class="table-tool-bar-left">
					<slot name="toolbar-left"></slot>
				</div>
				<div class="table-tool-bar-right">
					<el-space size="small" :spacer="spacer">
						<!-- 搜索 -->
						<icon-button size="16px" :el-icon="Search" :tooltip="showSearch ? '隐藏搜索' : '显示搜索'" @click="toggleSearch()" />
						<!-- 刷新 -->
						<icon-button size="16px" :el-icon="Refresh" tooltip="刷新表格" @click="emit('getDataList')" />
						<!-- 列设置 -->
						<el-popover
							v-model:visible="columnSettingsVisible"
							placement="top"
							:width="300"
							trigger="click"
							popper-class="yanggu-column-settings-popover"
							@show="handlePopoverShow"
						>
							<template #reference>
								<span class="column-settings-trigger">
									<icon-button size="16px" :el-icon="Setting" tooltip="列设置" />
								</span>
							</template>
						<column-settings-panel
							ref="columnSettingsRef"
							v-model:columns="columns"
							@reset="emit('resetColumns')"
						/>
						</el-popover>
						<!-- 最大化 -->
						<icon-button size="16px" :el-icon="FullScreen" :tooltip="maximized ? '退出全屏' : '表格全屏'" @click="handleToggleMaximized()" />
					</el-space>
				</div>
			</div>
		</template>

		<!-- 表格 -->
		<el-table
			ref="tableRef"
			v-loading="loading"
			v-bind="$attrs"
			:data="data"
			:max-height="maxHeight"
			border
			:row-key="rowKey"
			@selection-change="(s: any[]) => emit('selectionChange', s)"
			@sort-change="(s: any) => emit('sortChange', s)"
		>
			<template v-for="col in visibleColumns" :key="col.key">
				<!-- 分组列：父表头 + 内嵌叶子列（仅 2 层） -->
				<el-table-column v-if="col.children?.length" v-bind="toColumnProps(col)">
					<template v-if="col.tooltip" #header>
						<form-label-tooltip :label="col.label" :tooltip="col.tooltip" :align="col.headerAlign ?? col.align ?? 'center'" />
					</template>
					<el-table-column v-for="child in col.children" :key="child.key" v-bind="toColumnProps(child)">
						<template v-if="child.tooltip" #header>
							<form-label-tooltip :label="child.label" :tooltip="child.tooltip" :align="child.headerAlign ?? child.align ?? 'center'" />
						</template>
						<template v-if="child.key === 'operation'" #default="scope">
							<slot name="operation" v-bind="scope"></slot>
						</template>
						<template v-else-if="$slots[child.key]" #default="scope">
							<slot :name="child.key" v-bind="scope"></slot>
						</template>
					</el-table-column>
				</el-table-column>
				<!-- 顶层叶子列 -->
				<el-table-column v-else v-bind="toColumnProps(col)">
					<template v-if="col.tooltip" #header>
						<form-label-tooltip :label="col.label" :tooltip="col.tooltip" :align="col.headerAlign ?? col.align ?? 'center'" />
					</template>
					<template v-if="col.key === 'operation'" #default="scope">
						<slot name="operation" v-bind="scope"></slot>
					</template>
					<template v-else-if="$slots[col.key]" #default="scope">
						<slot :name="col.key" v-bind="scope"></slot>
					</template>
				</el-table-column>
			</template>
			<template #empty>
				<slot name="empty">
					<el-empty description="暂无数据" :image-size="120"></el-empty>
				</slot>
			</template>
		</el-table>

		<!-- 分页 -->
		<el-pagination
			v-if="pagination"
			:current-page="pagination.current"
			:page-sizes="pageSizes"
			:page-size="pagination.size"
			:total="pagination.total"
			background
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="(s: number) => emit('sizeChange', s)"
			@current-change="(p: number) => emit('pageChange', p)"
		></el-pagination>
	</el-card>
</template>

<script setup lang="ts">
import { FullScreen, Refresh, Search, Setting } from '@element-plus/icons-vue'
import { ElDivider } from 'element-plus'
import IconButton from '@/components/icon-button/index.vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import ColumnSettingsPanel from './internal/ColumnSettingsPanel.vue'
import { DEFAULT_PAGE_SIZES, DEFAULT_PRIMARY_KEY, DEFAULT_TABLE_COLUMN_OPTIONS, DEFAULT_TABLE_MAX_HEIGHT } from '@/config/crud'
import type { Pagination, TableFieldSchema } from '@/types/schema'

defineOptions({
	name: 'CrudDataTable',
	inheritAttrs: false
})

// ====== defineModel ======
const showSearch = defineModel('showSearch', { type: Boolean, default: true })
const maximized = defineModel('maximized', { type: Boolean, default: false })
const columns = defineModel<TableFieldSchema[]>('columns', { default: () => [] })

const props = withDefaults(
	defineProps<{
		data: any[]
		loading?: boolean
		pagination?: Pagination
		pageSizes?: number[]
		maxHeight?: string
		/**
		 * 序号列的行号计算函数，映射到 el-table-column 的 `index` 属性。
		 * el-table 的 type="index" 只按「当前页」的行下标从 1 起算，翻页后又从 1 开始，
		 * 必须由外部传入跨页序号函数（(pageNum-1)*pageSize+index+1）才能与分页条页码对齐。
		 */
		rowIndex?: (index: number) => number
		/** 行数据主键字段名，映射到 el-table 的 row-key（跳页保持勾选、树形数据行身份依赖它）。默认 'id' */
		rowKey?: string
		/** 是否显示卡片 header 区（工具栏整块，含左右两侧），默认 true */
		showTableHeader?: boolean
		/** 是否显示工具栏左侧（默认按钮或用户插槽）。默认 true */
		showToolbarLeft?: boolean
	}>(),
	{
		loading: false,
		pageSizes: () => [...DEFAULT_PAGE_SIZES],
		maxHeight: DEFAULT_TABLE_MAX_HEIGHT,
		// 不传时等同 el-table 原生行为：每页从 1 开始
		rowIndex: (index: number) => index + 1,
		rowKey: DEFAULT_PRIMARY_KEY,
		showTableHeader: true,
		showToolbarLeft: true
	}
)

const emit = defineEmits<{
	(e: 'selectionChange', selections: any[]): void
	(e: 'sortChange', sort: any): void
	(e: 'pageChange', page: number): void
	(e: 'sizeChange', size: number): void
	(e: 'getDataList'): void
	(e: 'resetColumns'): void
}>()

// ====== 工具栏 ======
const spacer = h(ElDivider, { direction: 'vertical' })
const toggleSearch = useToggle(showSearch)
const toggleMaximized = useToggle(maximized)

const handleToggleMaximized = () => {
	toggleMaximized()
	if (!maximized.value) {
		ElMessage.info({ message: '按 ESC 可退出最大化', duration: 1000 })
	}
}

onKeyStroke('Escape', () => {
	if (maximized.value) maximized.value = false
})

// ====== 可见列（递归：分组下无可见叶子时整组隐藏） ======
const isColumnVisible = (col: TableFieldSchema) => col.visible !== false
const visibleColumns = computed<TableFieldSchema[]>(() => {
	const result: TableFieldSchema[] = []
	for (const col of columns.value) {
		if (!isColumnVisible(col)) continue
		if (col.children?.length) {
			const children = col.children.filter(isColumnVisible)
			if (children.length) result.push({ ...col, children })
		} else {
			result.push(col)
		}
	}
	return result
})

// ====== 列设置面板 ======
const columnSettingsVisible = ref(false)
const columnSettingsRef = ref<InstanceType<typeof ColumnSettingsPanel>>()

const handlePopoverShow = () => {
	columnSettingsRef.value?.onPopoverShow()
}

const resetColumns = () => {
	columnSettingsRef.value?.resetState()
	emit('resetColumns')
}

// ====== 列配置转换 ======
// 默认值是只读常量：toColumnProps 在模板里逐列调用（render 期），不能每次调用都新建一份
const columnDefaults = DEFAULT_TABLE_COLUMN_OPTIONS

const toColumnProps = (col: TableFieldSchema): Record<string, any> => {
	const result: Record<string, any> = {}
	// 展开列：单元格是展开区的挂载点，prop / formatter / sortable / showOverflowTooltip / align
	// 等数据列属性对它均无消费点，只下发有实际意义的四项
	if (col.type === 'expand') {
		result.type = col.type
		if (col.label) result.label = col.label
		if (col.width) result.width = col.width
		if (col.fixed) result.fixed = col.fixed
		return result
	}
	// 分组列：仅作父表头，不下发 prop / formatter / showOverflowTooltip / sortable（对纯表头无意义）
	if (col.children?.length) {
		if (col.label) result.label = col.label
		if (col.width) result.width = col.width
		if (col.minWidth) result.minWidth = col.minWidth
		if (col.fixed) result.fixed = col.fixed
		result.align = col.align ?? columnDefaults.align
		if (col.headerAlign) result.headerAlign = col.headerAlign
		return result
	}
	if (col.type) {
		result.type = col.type
		// 序号列必须下发 index，否则 el-table 只按当前页下标从 1 数，第 2 页起仍是 1、2、3
		if (col.type === 'index') result.index = props.rowIndex
	}
	if (col.prop) result.prop = col.prop
	else if (col.key && !col.type) result.prop = col.key
	if (col.label) result.label = col.label
	if (col.width) result.width = col.width
	if (col.minWidth) result.minWidth = col.minWidth
	if (col.fixed) result.fixed = col.fixed
	result.align = col.align ?? columnDefaults.align
	if (col.headerAlign) result.headerAlign = col.headerAlign
	if (col.sortable) result.sortable = col.sortable
	if ((col.showOverflowTooltip ?? columnDefaults.showOverflowTooltip) !== false) result.showOverflowTooltip = true
	if (col.formatter) result.formatter = col.formatter
	if (col.reserveSelection) result.reserveSelection = true
	return result
}

// ====== Refs ======
const tableRef = ref()
const cardRef = ref()

defineExpose({ tableRef, cardRef })
</script>

<style scoped>
/* ====== 工具栏 ====== */
.table-tool-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.table-tool-bar-left {
	display: flex;
	align-items: center;
}

.table-tool-bar-right {
	display: flex;
	align-items: center;
	margin-left: auto;
}
.table-tool-bar-right :deep(.icon-button) {
	background-color: var(--el-fill-color);
	border-radius: 6px;
	padding: 6px;
}
.table-tool-bar-right :deep(.icon-button:not(.is-disabled):hover) {
	background-color: var(--el-fill-color-dark);
}
:deep(.el-divider--vertical) {
	margin: 0 0;
}

/* ====== 列设置触发器 ====== */
.column-settings-trigger {
	display: inline-flex;
	align-items: center;
}
</style>
