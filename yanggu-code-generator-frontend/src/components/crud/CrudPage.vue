<template>
	<div>
		<query-table :table-hook="tableHook">
			<!-- 工具栏左侧：默认新增+批量删除（可被 #table-toolbar-left 覆盖） -->
			<template #toolbar-left="{ tableHook: t }">
				<slot name="table-toolbar-left" :table-hook="t" :dialog="crudDialogRef">
					<el-space v-if="!tableSlots['table-toolbar-left']" size="default">
						<el-button type="primary" :icon="Plus" @click="crudDialogRef?.open('add')">新增</el-button>
						<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">删除</el-button>
					</el-space>
				</slot>
			</template>
			<!-- 操作列：默认修改+行删除（可被 #table-action 覆盖） -->
			<template #operation="{ row, tableHook: t }">
				<slot name="table-action" :row="row" :table-hook="t" :dialog="crudDialogRef">
					<template v-if="!tableSlots['table-action']">
						<el-button type="primary" link :icon="Edit" @click="crudDialogRef?.open('update', row[t.refs.primaryKey])">修改</el-button>
						<el-button type="primary" link :icon="Delete" @click="t.action.delete.execute(row)">删除</el-button>
					</template>
				</slot>
			</template>
			<!-- 透传表格列插槽（保留 table- 前缀，由 QueryTable 剥前缀后转给 DataTable） -->
			<template v-for="(fn, name) in tableSlots" :key="name" #[name]="slotData">
				<component :is="() => fn(slotData ?? {})" />
			</template>
			<!-- 透传搜索字段插槽（保留 query- 前缀，由 QueryTable 剥前缀后转给 SearchForm） -->
			<template v-for="(fn, name) in querySlots" :key="name" #[name]="slotData">
				<component :is="() => fn(slotData ?? {})" />
			</template>
		</query-table>

		<!-- 弹窗表单 -->
		<crud-dialog ref="crudDialogRef" :form-hook="formHook">
			<!-- 透传表单字段插槽（已剥去 form- 前缀，补 field- 前缀匹配 FormField） -->
			<template v-for="(fn, name) in formSlots" :key="name" #[name]="slotData">
				<component :is="() => fn(slotData ?? {})" />
			</template>
		</crud-dialog>
	</div>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import QueryTable from './QueryTable.vue'
import CrudDialog from './CrudDialog.vue'
import { useCrudForm, useCrudTable } from '@/hooks'

defineOptions({
	name: 'CrudPage'
})

// ====== Props ======
// 注意：useCrudTable / useCrudForm 是泛型函数，不同页面的 VO 类型不同，
defineProps<{
	tableHook: ReturnType<typeof useCrudTable>
	formHook: ReturnType<typeof useCrudForm>
}>()

// ====== 引用 ======
const crudDialogRef = ref<InstanceType<typeof CrudDialog>>()

// ====== 插槽路由（按前缀分类 → 保留前缀分发到 QueryTable，由 QueryTable 剥前缀给子组件） ======
//
// 命名约定（统一用 key 作为插槽名第二段）：
//   table-toolbar-left   → 工具栏左侧（显式处理，默认新增+批量删除）
//   table-action         → 操作列（显式处理，默认修改+行删除）
//   table-[列key]        → 表格列自定义渲染（保留 table- 前缀转发，QueryTable 剥前缀给 DataTable）
//   query-[字段key]      → 搜索字段自定义（保留 query- 前缀转发，QueryTable 剥前缀给 SearchForm）
//   form-[字段key]       → 表单字段自定义（剥成字段 key，补 field- 转发给 CrudDialog → FormField）
const allSlots = useSlots()

// 已被显式 template 处理的插槽名（不进入动态转发，避免重复注册）
const handledSlotNames = new Set(['table-toolbar-left', 'table-action'])

/** table-* 前缀的插槽：保留原始名称，转发给 QueryTable（由 QueryTable 剥 table- 给 DataTable） */
const tableSlots = computed(() => {
	const result: Record<string, any> = {}
	for (const name in allSlots) {
		if (!name.startsWith('table-')) {
			continue
		}
		if (!handledSlotNames.has(name)) {
			result[name] = allSlots[name]
		}
	}
	return result
})

/** query-* 前缀的插槽：保留原始名称，转发给 QueryTable（由 QueryTable 剥 query- 给 SearchForm） */
const querySlots = computed(() => {
	const result: Record<string, any> = {}
	for (const name in allSlots) {
		if (name.startsWith('query-') && !handledSlotNames.has(name)) {
			result[name] = allSlots[name]
		}
	}
	return result
})

/** form-* 前缀的插槽：剥去 form- 前缀，补 field- 前缀匹配 FormField 的插槽名 */
const formSlots = computed(() => {
	const result: Record<string, any> = {}
	for (const name in allSlots) {
		if (name.startsWith('form-') && !handledSlotNames.has(name)) {
			result[`field-${name.slice(5)}`] = allSlots[name]
		}
	}
	return result
})
</script>
