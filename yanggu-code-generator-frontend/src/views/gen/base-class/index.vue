<template>
	<crud-page :table-hook="tableHook" :form-hook="formHook">
		<!-- 表格列 fieldList：PopoverList 展示 -->
		<template #table-fieldList="{ row }">
			<popover-list :list="row.fieldList" :width="250" :preview-count="2" />
		</template>

		<!-- 操作列：详情 + 复制 + 修改 + 删除（2×2 网格） -->
		<template #table-action="{ row, tableHook: t, dialog }">
			<el-row>
				<el-col :span="12">
					<el-button type="primary" link :icon="View" @click="dialog?.open('detail', row[t.refs.primaryKey])">详情</el-button>
				</el-col>
				<el-col :span="12">
					<el-button type="primary" link :icon="Edit" @click="dialog?.open('update', row[t.refs.primaryKey])">修改</el-button>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-button type="primary" link :icon="CopyDocument" @click="dialog?.open('copy', row[t.refs.primaryKey])">复制</el-button>
				</el-col>
				<el-col :span="12">
					<el-button type="primary" link :icon="Delete" @click="t.action.delete.execute(row)">删除</el-button>
				</el-col>
			</el-row>
		</template>

		<!-- 表单字段 fullClassName：包名 + 类名组合布局 -->
		<template #form-fullClassName="{ model, mode }">
			<div class="full-class-name">
				<el-form-item prop="packageName" class="inner-form-item">
					<el-input v-model="model.packageName" :disabled="mode === 'detail'" placeholder="请输入基类包名" clearable style="flex: 1" />
				</el-form-item>
				<span class="dot">.</span>
				<el-form-item prop="className" class="inner-form-item">
					<el-input v-model="model.className" :disabled="mode === 'detail'" placeholder="请输入基类类名" clearable style="flex: 1" />
				</el-form-item>
			</div>
		</template>

		<!-- 表单字段 fieldList：el-input-tag -->
		<template #form-fieldList="{ model }">
			<el-input-tag
				v-model="model.fieldList"
				collapse-tags
				collapse-tags-tooltip
				:max-collapse-tags="5"
				draggable
				clearable
				placeholder="请输入基类字段"
			></el-input-tag>
		</template>
	</crud-page>
</template>

<script setup lang="ts">
import { genBaseClassApi } from '@/api'
import type { FormFieldSchema, SearchFieldSchema, TableColumnSchema } from '@/types/schema'
import type { GenBaseClassEntity, GenBaseClassQuery, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'
import PopoverList from '@/components/popover/list/index.vue'
import { CopyDocument, Delete, Edit, View } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenBaseClass'
})

// 初始化表单数据
const initFormData = (): GenBaseClassEntity => ({
	id: '',
	baseClassName: '',
	packageName: '',
	className: '',
	fieldList: [],
	remark: ''
})

// 表单字段（packageName / className 合并为「基类全类名」组合布局，走 #form-fullClassName 插槽）
const formSchema: FormFieldSchema[] = [
	{
		key: 'baseClassName',
		label: '基类名称',
		tooltip: '基类名称具有唯一性，不能重复',
		ruleList: [{ required: true, message: '基类名称不能为空', trigger: 'blur' }]
	},
	{
		key: 'fullClassName',
		label: '基类全类名',
		tooltip: '基类全类名具有唯一性，不能重复',
		ruleList: [
			{
				required: true,
				validator: (_: any, __: any, callback: any) => callback(),
				message: '基类全类名不能为空',
				trigger: 'blur'
			}
		]
	},
	{
		key: 'fieldList',
		label: '基类字段',
		ruleList: [{ required: true, message: '基类字段不能为空', trigger: 'blur' }]
	},
	{
		key: 'remark',
		label: '备注'
	}
]

// 表单配置
const formConfig = reactive({
	formSchema,
	submitApi: genBaseClassApi.submit,
	detailApi: genBaseClassApi.detail,
	initFormData,
	rules: {
		packageName: [{ required: true, message: '基类包名不能为空', trigger: 'blur' }],
		className: [{ required: true, message: '基类类名不能为空', trigger: 'blur' }]
	},
	labelWidth: '130px'
} as UseCrudFormOptions)

// 搜索字段
const searchSchema: SearchFieldSchema[] = [
	{ key: 'baseClassName', label: '基类名称' },
	{ key: 'packageName', label: '基类包名' },
	{ key: 'className', label: '基类类名' }
]

// 初始化查询表单数据
const initQueryForm = (): GenBaseClassQuery => ({
	baseClassName: '',
	packageName: '',
	className: ''
})

// 表格列
const tableColumns: TableColumnSchema[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'baseClassName', label: '基类名称', align: 'center', showOverflowTooltip: true },
	{ key: 'packageName', label: '基类包名', align: 'center', showOverflowTooltip: true },
	{ key: 'className', label: '基类类名', align: 'center', showOverflowTooltip: true },
	{ key: 'fieldList', label: '基类字段', align: 'center', minWidth: 210 },
	{ key: 'remark', label: '备注', align: 'center', showOverflowTooltip: true },
	{ key: 'createTime', label: '创建时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'updateTime', label: '修改时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'operation', label: '操作', fixed: 'right' as const, width: 150, align: 'center', disabled: true }
]

// 表格配置
const tableConfig = reactive({
	initQueryForm,
	searchSchema,
	tableColumns,
	dataListApi: genBaseClassApi.entityPage,
	deleteApi: genBaseClassApi.deleteList,
	deleteNameKey: 'baseClassName',
	tableKey: 'gen-base-class'
} as UseCrudTableOptions)

// 统一 hook
const { tableHook, formHook } = useCrud<GenBaseClassEntity, GenBaseClassQuery, GenBaseClassEntity>({
	primaryKey: 'id',
	subject: '基类',
	tableConfig,
	formConfig
})

// 复制模式：基类名称追加"_复制"，清除主键
formConfig.afterOpen = () => {
	if (formHook.dialog.mode === 'copy') {
		formHook.form.data.baseClassName += '_复制'
		formHook.form.data.id = ''
	}
}
</script>

<style lang="scss" scoped>
.full-class-name {
	display: flex;
	align-items: center;
	width: 100%;
	gap: 8px;
}

/* 子 form-item 去掉自身的布局副作用 */
.inner-form-item {
	flex: 1;
	margin-bottom: 0; /* 去掉多余行距 */
}

.inner-form-item .el-form-item__content {
	margin-left: 0 !important; /* 去掉 label 占位 */
}

.dot {
	flex-shrink: 0;
}
</style>
