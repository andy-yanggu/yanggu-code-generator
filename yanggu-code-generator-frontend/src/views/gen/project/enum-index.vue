<template>
	<!-- @expand-change 不在 QueryTable 的 emits 声明里，经新补的 $attrs 透传通道直达 el-table -->
	<query-table ref="queryTableRef" :table-hook="tableHook" @selection-change="onSelectionChange" @expand-change="onExpandChange">
		<!-- 展开区：枚举项渲染为彩色标签（行数据的 tagType/tagEffect/tagColor 驱动，与枚举项页预览列同构；
		     'default' 是 DB 列默认值，归一为 undefined 走 el-tag 默认样式） -->
		<template #table-enumItems="{ row }">
			<div v-if="enumItemsMap[row.id]" class="enum-items-expand">
				<el-tag
					v-for="(item, i) in enumItemsMap[row.id]"
					:key="i"
					:type="resolveTagType(item)"
					:effect="resolveTagEffect(item)"
					:color="item.tagColor || undefined"
				>{{ item.enumItemDesc }}（{{ item.enumItemCode }}）</el-tag>
				<el-text v-if="!enumItemsMap[row.id].length" type="info">暂无枚举项</el-text>
			</div>
			<el-text v-else class="enum-items-expand" type="info">枚举项加载中…</el-text>
		</template>
	</query-table>
</template>

<script setup lang="ts">
import { genEnumApi, genEnumItemApi } from '@/api'
import type { GenEnumEntity, GenEnumItemEntity, GenEnumQuery, SearchFieldSchema, TableFieldSchema, UseCrudTableOptions } from '@/types'
import type { TagEffect, TagType } from '@/constant/enum'
import { createBuiltinCreateTime, createBuiltinIndex, createBuiltinSelection, createBuiltinUpdateTime, useCrudTable } from '@/hooks'
import QueryTable from '@/components/crud/QueryTable.vue'

defineOptions({
	name: 'GenProjectEnum'
})

const props = defineProps<{
	selectedIds?: number[]
}>()

const emit = defineEmits(['selectChange'])
const queryTableRef = ref()

// 搜索字段
const searchFields: SearchFieldSchema[] = [{ key: 'enumName', label: '枚举名称' }]

// 表格列配置
const tableColumns: TableFieldSchema[] = [
	// 展开列：#table-enumItems 插槽在展开区就地显示该枚举的项（取代原弹窗）；
	// 与 selection/index 同为固定左侧的结构列，横向滚动时展开入口不滚走
	{ key: 'enumItems', type: 'expand', label: '', fixed: 'left', disabled: true },
	createBuiltinSelection(),
	createBuiltinIndex(),
	{ key: 'enumName', label: '枚举名称' },
	{ key: 'enumDesc', label: '枚举描述' },
	createBuiltinCreateTime(),
	createBuiltinUpdateTime()
]

// 查询上下文（响应式，init 时更新 projectId）
const queryContext = reactive({
	projectId: '' as number | string
})

// 初始化查询表单数据
const initQueryData = (): GenEnumQuery => ({
	enumName: '',
	projectId: ''
})

// 表格配置
const tableHook = useCrudTable<GenEnumEntity, GenEnumQuery>({
	dataListApi: genEnumApi.entityPage,
	tableKey: 'gen-project-enum',
	initQueryData,
	searchSchema: searchFields,
	tableColumns,
	queryContext,
	mountedGetData: false,
	subject: '枚举',
	showTableHeader: false
} as UseCrudTableOptions)

// 勾选变化：转发给父组件
const onSelectionChange = (selections: any[]) => {
	emit('selectChange', selections)
}

// 数据加载后，根据 selectedIds 自动恢复勾选
watch(
	() => tableHook.table.data,
	data => {
		if (data.length === 0 || !props.selectedIds?.length) return
		const elTableRef = queryTableRef.value?.tableRef
		if (!elTableRef) return
		const rows = data.filter((row: any) => props.selectedIds!.includes(row.id))
		rows.forEach((row: any) => elTableRef.toggleRowSelection(row, true))
	},
	{ flush: 'post' }
)

// 对外暴露的初始化方法
const init = (projectId: number) => {
	queryContext.projectId = projectId
	tableHook.query.reset()
}

// 展开区枚举项缓存：首次展开时按 enumId 一次拉全（entityList 不分页），后续展开命中缓存
const enumItemsMap = reactive<Record<string, GenEnumItemEntity[]>>({})

// el-tag 绑定归一：DB 行数据的 tag 字段是 string | null，且列默认值 'default' 不在 el-tag 白名单里，
// 收敛成 EP 的字面量联合（未配置/非法值统一 undefined 走默认样式）
const resolveTagType = (item: GenEnumItemEntity) => (item.tagType && item.tagType !== 'default' ? (item.tagType as TagType) : undefined)
const resolveTagEffect = (item: GenEnumItemEntity) => (item.tagEffect ? (item.tagEffect as TagEffect) : undefined)

const onExpandChange = (row: GenEnumEntity, expandedRows: GenEnumEntity[]) => {
	// GenEnumEntity.id 声明为 Key（含 null/undefined 分支），真实数据必为数字主键，此处收敛索引类型
	const enumId = row.id as number
	if (!expandedRows.includes(row) || enumItemsMap[enumId]) return
	genEnumItemApi.entityList({ enumId }).then(list => {
		enumItemsMap[enumId] = list
	})
}

defineExpose({
	init
})
</script>

<style scoped>
/* 展开区枚举项标签布局 */
.enum-items-expand {
	flex-wrap: wrap;
	display: flex;
	gap: 8px;
	padding: 4px 16px;
}
</style>
