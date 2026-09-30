<!--
	ConfigTable：基于 columns 配置驱动的表格组件
	- 从 columns 数组自动渲染 el-table-column 循环
	- 按 column.key 透传命名插槽
	- 内置 el-pagination（可选，通过 pagination prop 控制）
	- 透传 ElTable 所有原生 props/events/slots
-->
<template>
	<div class="config-table">
		<el-table ref="tableRef" v-loading="loading" v-bind="$attrs" :data="data">
			<el-table-column v-for="col in columns" :key="col.key" v-bind="toColumnProps(col)">
				<!-- 透传命名插槽：以 column.key 为插槽名 -->
				<template v-if="$slots[col.key]" #default="scope">
					<slot :name="col.key" v-bind="scope" />
				</template>
			</el-table-column>
			<!-- 空数据插槽 -->
			<template #empty>
				<slot name="empty">
					<el-empty description="暂无数据" :image-size="120" />
				</slot>
			</template>
		</el-table>

		<!-- 分页（可选） -->
		<el-pagination
			v-if="pagination"
			:current-page="pagination.current"
			:page-sizes="pageSizes"
			:page-size="pagination.size"
			:total="pagination.total"
			background
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="handleSizeChange"
			@current-change="handleCurrentChange"
		/>
	</div>
</template>

<script setup lang="ts">
import type { ColumnConfig } from '@/types/hooks/column-config'

defineOptions({
	name: 'ConfigTable',
	// 使 ElTable 的 $attrs（如 @selection-change、@sort-change）能正确透传
	inheritAttrs: false
})

const props = withDefaults(
	defineProps<{
		/** 列配置 */
		columns: ColumnConfig[]
		/** 表格数据 */
		data: any[]
		/** 加载状态 */
		loading?: boolean

		// ---- 分页（可选） ----
		/** 分页参数，不传则不显示分页 */
		pagination?: { current: number; size: number; total: number }
		/** 每页条数选项 */
		pageSizes?: number[]
	}>(),
	{
		loading: false,
		pageSizes: () => [10, 20, 50, 100, 200]
	}
)

const emit = defineEmits<{
	/** 每页条数变化 */
	(e: 'size-change', size: number): void
	/** 页码变化 */
	(e: 'page-change', page: number): void
}>()

const tableRef = ref()

/** 将 ColumnConfig 转换为 ElTableColumn 的 props */
const toColumnProps = (col: ColumnConfig): Record<string, any> => {
	const result: Record<string, any> = {}
	if (col.type) result.type = col.type
	if (col.prop) result.prop = col.prop
	if (col.label) result.label = col.label
	if (col.width) result.width = col.width
	if (col.minWidth) result.minWidth = col.minWidth
	if (col.fixed) result.fixed = col.fixed
	if (col.align) result.align = col.align
	if (col.headerAlign) result.headerAlign = col.headerAlign
	if (col.sortable) result.sortable = col.sortable
	// 默认开启超出提示，显式传 false 时关闭
	if (col.showOverflowTooltip !== false) result.showOverflowTooltip = true
	if (col.formatter) result.formatter = col.formatter
	return result
}

const handleSizeChange = (size: number) => {
	emit('size-change', size)
}

const handleCurrentChange = (page: number) => {
	emit('page-change', page)
}

/** 暴露 tableRef 供父组件调用 ElTable 方法（如 clearSelection） */
defineExpose({
	tableRef
})
</script>

<style scoped>
.config-table {
	display: flex;
	flex-direction: column;
}

.config-table :deep(.el-pagination) {
	display: flex;
	justify-content: flex-end;
	margin-top: 12px;
}
</style>
