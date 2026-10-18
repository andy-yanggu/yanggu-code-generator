<template>
	<div>
		<query-table :table-hook="tableHook">
			<!-- 工具栏左侧：仅当需要时传递（用户提供了 #table-toolbar-left 插槽 或 showToolbarLeft 为 true） -->
			<template v-if="tableHook.refs.showToolbarLeft" #toolbar-left="{ tableHook: t }">
				<slot name="table-toolbar-left" :table-hook="t" :dialog="crudDialogRef">
					<el-space size="default">
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
						<el-button type="danger" link :icon="Delete" @click="t.action.delete.execute(row)">删除</el-button>
					</template>
				</slot>
			</template>
			<!-- 透传表格列插槽（保留 table- 前缀，由 QueryTable 剥前缀后转给 DataTable）
			     注意：必须用 <slot> 直接渲染。若写成 <component :is="() => fn(scope)">，
			     每次 render 都会产生新的函数式组件类型，patch 时按不同类型处理而卸载重建，
			     插槽里的 el-select 展开态、输入焦点、局部 state 会全部丢失。 -->
			<template v-for="(_, name) in tableSlots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}"></slot>
			</template>
			<!-- 透传搜索字段插槽（保留 query- 前缀，由 QueryTable 剥前缀后转给 SearchForm） -->
			<template v-for="(_, name) in querySlots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}"></slot>
			</template>
		</query-table>

		<!-- 弹窗表单 -->
		<crud-dialog ref="crudDialogRef" :form-hook="formHook">
			<!-- 透传表单字段插槽（form-xxx → field-xxx 匹配 FormField）
			     两个名字不能混用：#[s.field] 是对外插槽名，
			     <slot :name="s.full"> 才是本组件 $slots 的真实键名（带 form- 前缀），
			     写成 <slot :name="s.field"> 会匹配不到而渲染成空注释。 -->
			<template v-for="s in formSlots" :key="s.full" #[s.field]="slotData">
				<slot :name="s.full" v-bind="slotData ?? {}"></slot>
			</template>
		</crud-dialog>
	</div>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import QueryTable from './QueryTable.vue'
import CrudDialog from './CrudDialog.vue'
import type { UseCrudFormReturn, UseCrudTableReturn } from '@/types'

defineOptions({
	name: 'CrudPage'
})

// ====== Props ======
// 用具名契约类型而不是 ReturnType<typeof useCrudTable>：后者拿到的是未实例化的泛型签名，
// 只能用默认实参展开成 <any, any>，与页面侧的具体实体实例化结果比较时会报 TS2719。
// 具名接口有稳定的类型身份，默认实参下各属性退化为 any，页面传具体类型仍可直接赋值。
defineProps<{
	tableHook: UseCrudTableReturn
	formHook: UseCrudFormReturn
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

/**
 * form-* 前缀的插槽：form-xxx 改名成 field-xxx 匹配 FormField。
 * full 是本组件 $slots 的真实键名（渲染 `<slot :name>` 必须用它），field 是给 CrudDialog 的插槽名。
 * 只留 field 会让 `<slot :name>` 找不到自己的插槽，表单自定义字段会退化成默认控件。
 */
const formSlots = computed(() =>
	Object.keys(allSlots)
		.filter(name => name.startsWith('form-') && !handledSlotNames.has(name))
		.map(full => ({ full, field: `field-${full.slice(5)}` }))
)
</script>
