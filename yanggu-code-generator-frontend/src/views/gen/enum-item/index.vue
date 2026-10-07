<template>
	<el-dialog v-model="dialogVisible" :title="`枚举项配置（${enumNameRef}）`" width="80%" @close="closeHandler">
		<crud-page :table-hook="tableHook" :form-hook="formHook">
			<!-- 表单字段 enumItemOrder：input-number 带 min=0 -->
			<template #form-enumItemOrder="{ model, mode }">
				<el-input-number v-model="model.enumItemOrder" :min="0" :disabled="mode === 'detail'" size="small"></el-input-number>
			</template>
		</crud-page>
	</el-dialog>
</template>

<script setup lang="ts">
import { genEnumItemApi } from '@/api'
import type { CrudField, GenEnumItemEntity, GenEnumItemQuery, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'

defineOptions({
	name: 'GenEnumItem'
})

const emit = defineEmits(['refresh-data-list'])

// 统一字段声明
const fields: CrudField[] = [
	{
		key: 'enumItemName',
		label: '枚举项名称',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			tooltip: "使用英文小写字母，单词之间使用'-'拼接；该字段具有唯一性",
			ruleList: [{ required: true, message: '枚举项名称不能为空', trigger: 'blur' }]
		}
	},
	{
		key: 'enumItemCode',
		label: '枚举项编码',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			tooltip: '枚举项编码具有唯一性',
			ruleList: [{ required: true, message: '枚举项编码不能为空', trigger: 'blur' }]
		}
	},
	{
		key: 'enumItemDesc',
		label: '枚举项描述',
		inForm: true,
		inTable: true,
		form: {
			ruleList: [{ required: true, message: '枚举项描述不能为空', trigger: 'blur' }]
		}
	},
	{
		key: 'enumItemOrder',
		label: '枚举项排序',
		inForm: true,
		inTable: true,
		component: 'input-number',
		form: {
			ruleList: [{ required: true, message: '枚举项排序不能为空', trigger: 'blur' }]
		},
		table: { width: 120, sortable: 'custom' }
	}
]

// 枚举名称与对话框状态
const enumNameRef = ref('')
const dialogVisible = ref(false)

// 表单上下文（响应式，init 时更新 enumId，自动注入到 initFormData / beforeOpen）
const formContext = reactive({
	enumId: -1
})

// 初始化表单数据
const initFormData = (ctx?: Record<string, any>): GenEnumItemEntity => ({
	id: '',
	enumId: ctx?.enumId ?? -1,
	enumItemName: '',
	enumItemCode: '',
	enumItemDesc: '',
	enumItemOrder: 0
})

// 表单配置
const formConfig = reactive({
	initFormData,
	submitApi: genEnumItemApi.submit,
	detailApi: genEnumItemApi.detail,
	afterDataAssign: () => {
		// 确保 enumId 始终正确（编辑/复制/详情模式下从接口赋值后补回）
		;(formHook.form.data as GenEnumItemEntity).enumId = formContext.enumId
		if (formHook.dialog.mode === 'copy') {
			formHook.form.data.enumItemName += '_复制'
			formHook.form.data.id = ''
		}
	},
	labelWidth: '120px',
	formContext
} as UseCrudFormOptions)

// 初始化查询表单数据
const initQueryForm = (): GenEnumItemQuery => ({
	enumItemName: '',
	enumItemCode: '',
	enumId: -1
})

// 查询上下文（响应式，init 时更新 enumId）
const queryContext = reactive({
	enumId: -1
})

// 表格配置
const tableConfig = reactive({
	initQueryForm,
	dataListApi: genEnumItemApi.entityPage,
	deleteApi: genEnumItemApi.deleteList,
	deleteNameKey: 'enumItemName',
	tableKey: 'gen-enum-item',
	queryContext,
	mountedGetData: false
} as UseCrudTableOptions)

// 统一 hook
const { tableHook, formHook } = useCrud<GenEnumItemEntity, GenEnumItemQuery, GenEnumItemEntity>({
	subject: '枚举项',
	fields,
	tableConfig,
	formConfig
})

// 对外暴露的初始化方法
const init = (enumId: number, enumName: string) => {
	formContext.enumId = enumId
	enumNameRef.value = enumName
	queryContext.enumId = enumId
	dialogVisible.value = true
	tableHook.table.data = []
	tableHook.query.reset()
}

const closeHandler = () => {
	dialogVisible.value = false
	emit('refresh-data-list')
}

defineExpose({
	init
})
</script>
