<template>
	<div>
		<query-table ref="queryTableRef" v-bind="tableDialogProps">
			<!-- 工具栏左侧：默认新增+批量删除 -->
			<template #toolbar-left="{ table }">
				<slot name="toolbar-left" :table="table" :dialog="crudDialogRef">
					<el-space size="default">
						<el-button type="primary" :icon="Plus" @click="crudDialogRef?.open('add')">新增</el-button>
						<el-button type="danger" :loading="table.delete.loading" :icon="Delete" @click="table.delete.execute()">删除</el-button>
					</el-space>
				</slot>
			</template>
			<!-- 操作列：默认修改+行删除 -->
			<template #operation="{ row, table }">
				<slot name="action-column" :row="row" :table="table" :dialog="crudDialogRef">
					<el-button type="primary" link :icon="Edit" @click="crudDialogRef?.open('update', row[primaryKey])">修改</el-button>
					<el-button type="primary" link :icon="Delete" @click="table.delete.execute(row)">删除</el-button>
				</slot>
			</template>
		</query-table>

		<!-- 弹窗表单 -->
		<crud-dialog ref="crudDialogRef" v-bind="formDialogProps">
			<!-- 透传所有插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
			<template v-for="(_, name) in dialogSlots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}"></slot>
			</template>
		</crud-dialog>
	</div>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import type { UseCrudFormOptions } from '@/types/hooks/use-crud-form'
import type { UseCrudTableOptions } from '@/types/hooks/use-crud-table'
import QueryTable from './QueryTable.vue'
import CrudDialog from './CrudDialog.vue'

defineOptions({
	name: 'CrudPage',
	Generic: ['VO', 'Query', 'DTO']
})

// ====== Props ======
interface CrudPageProps<VO = any, Query = any, DTO = any> extends UseCrudFormOptions<DTO, VO>, UseCrudTableOptions<VO, Query> {
	// 无额外属性，只是合并两个接口
}

const props = withDefaults(defineProps<CrudPageProps>(), {
	searchSchema: () => [],
	pageSize: 10,
	pageSizes: () => [10, 20, 50, 100, 200],
	subject: '数据',
	primaryKey: 'id',
	labelWidth: '100px'
})

// ====== 引用 ======
const queryTableRef = ref<InstanceType<typeof QueryTable>>()
const crudDialogRef = ref<InstanceType<typeof CrudDialog>>()

// ====== 表格配置透传 ======
const tableOptionKeys: (keyof UseCrudTableOptions)[] = [
	'dataListApi',
	'deleteApi',
	'initQueryForm',
	'columns',
	'searchSchema',
	'pageSize',
	'pageSizes',
	'primaryKey',
	'subject',
	'deleteNameKey',
	'tableKey'
]

const tableDialogProps = computed((): UseCrudTableOptions => {
	const result: Record<string, any> = {}
	for (const key of tableOptionKeys) {
		if (props[key] !== undefined) {
			result[key] = props[key]
		}
	}
	return result as UseCrudTableOptions
})

// ====== 表单配置透传 ======
const formOptionKeys: (keyof UseCrudFormOptions)[] = [
	'formSchema',
	'submitApi',
	'detailApi',
	'initFormData',
	'subject',
	'title',
	'rules',
	'labelWidth',
	'successMessage',
	'beforeOpen',
	'afterOpen',
	'beforeDataAssign',
	'beforeSubmit',
	'afterSubmit',
	'onDetailError',
	'onSubmitError'
]

const formDialogProps = computed((): UseCrudFormOptions => {
	const result: Record<string, any> = {}
	for (const key of formOptionKeys) {
		if (props[key] !== undefined) {
			result[key] = props[key]
		}
	}
	// 默认提交后刷新表格
	result.afterSubmit = props.afterSubmit ?? (() => queryTableRef.value?.tableHooks.query.getDataList())
	return result as UseCrudFormOptions
})

// ====== 插槽透传计算 ======
const reservedSlots = new Set(['toolbar-left', 'action-column'])
const dialogSlots = computed(() => {
	const slots: Record<string, any> = {}
	for (const name in useSlots()) {
		if (!reservedSlots.has(name)) {
			slots[name] = true
		}
	}
	return slots
})
</script>
