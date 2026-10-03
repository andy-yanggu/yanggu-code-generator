<template>
	<div>
		<query-table :table-hook="tableHook">
			<!-- 工具栏左侧：默认新增+批量删除 -->
			<template #toolbar-left="{ tableHook: t }">
				<slot name="toolbar-left" :table-hook="t" :dialog="crudDialogRef">
					<el-space size="default">
						<el-button type="primary" :icon="Plus" @click="crudDialogRef?.open('add')">新增</el-button>
						<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">删除</el-button>
					</el-space>
				</slot>
			</template>
			<!-- 操作列：默认修改+行删除 -->
			<template #operation="{ row, tableHook: t }">
				<slot name="action-column" :row="row" :table-hook="t" :dialog="crudDialogRef">
					<el-button type="primary" link :icon="Edit" @click="crudDialogRef?.open('update', row[t.refs.primaryKey])">修改</el-button>
					<el-button type="primary" link :icon="Delete" @click="t.action.delete.execute(row)">删除</el-button>
				</slot>
			</template>
		</query-table>

		<!-- 弹窗表单 -->
		<crud-dialog ref="crudDialogRef" :form-hook="formHook">
			<!-- 透传所有插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
			<template v-for="(_, name) in dialogSlots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}"></slot>
			</template>
		</crud-dialog>
	</div>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import type { useCrudTable } from '@/hooks/use-crud-table'
import type { useCrudForm } from '@/hooks/use-crud-form'
import QueryTable from './QueryTable.vue'
import CrudDialog from './CrudDialog.vue'

defineOptions({
	name: 'CrudPage'
})

// ====== Props ======
defineProps<{
	tableHook: ReturnType<typeof useCrudTable>
	formHook: ReturnType<typeof useCrudForm>
}>()

// ====== 引用 ======
const crudDialogRef = ref<InstanceType<typeof CrudDialog>>()

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
