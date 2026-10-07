<template>
	<el-dialog v-model="visible" :title="`属性配置（${templateGroupName}）`" :close-on-click-modal="false" width="80%" @close="closeHandle">
		<crud-page :table-hook="tableHook" :form-hook="formHook">
			<!-- 工具栏：导入需传 templateGroupId -->
			<template #table-toolbar-left="{ tableHook: t, dialog }">
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="dialog?.open('add')">新增</el-button>
					<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">删除</el-button>
					<el-upload :limit="1" :show-file-list="false" :http-request="({ file }) => t.action.import.execute(file, { templateGroupId })">
						<el-button type="success" :icon="Upload">导入</el-button>
					</el-upload>
					<el-button type="info" :icon="Download" @click="t.action.export.execute()">导出</el-button>
				</el-space>
			</template>

			<!-- 是否必填列：开关 -->
			<template #table-required="{ row }">
				<el-switch
					v-model="row.required"
					:loading="switchSubmitLoading"
					:active-value="activeValue"
					:inactive-value="inactiveValue"
					:active-text="activeText"
					:inactive-text="inactiveText"
					inline-prompt
					@change="
						(val: number) => {
							if (!row?.id || !row?.propTitle) {
								return
							}
							switchSubmitHandler(val, row)
						}
					"
				></el-switch>
			</template>

			<!-- 排序列：输入数字 -->
			<template #table-propOrder="{ row }">
				<el-input-number
					v-model="row.propOrder"
					:disabled="tableHook.table.loading"
					:min="0"
					size="small"
					@focus="row._oldPropOrder = row.propOrder"
					@change="(cur: number, old: number) => orderChangeHandle(cur, old, row)"
				></el-input-number>
			</template>

			<!-- 表单字段 required：number ↔ boolean 转换 -->
			<template #form-required="{ model }">
				<el-switch
					:model-value="model.required === 1"
					active-text="是"
					inactive-text="否"
					inline-prompt
					@update:model-value="(val: boolean) => (model.required = val ? 1 : 0)"
				/>
			</template>

			<!-- 表单字段 componentOptions：动态选项列表 -->
			<template #form-componentOptions="{ model }">
				<el-row
					v-for="(item, index) in model.componentOptions"
					:key="index"
					:gutter="10"
					:style="{ marginBottom: index < model.componentOptions.length - 1 ? '18px' : '0' }"
				>
					<el-col :span="10">
						<el-form-item :prop="`componentOptions[${index}].label`">
							<el-input v-model="item.label" clearable placeholder="请输入选项标题" style="width: 220px" />
						</el-form-item>
					</el-col>
					<el-col :span="10">
						<el-form-item :prop="`componentOptions[${index}].value`">
							<el-input v-model="item.value" clearable placeholder="请输入选项值" style="width: 220px" />
						</el-form-item>
					</el-col>
					<div class="module-actions">
						<el-button circle type="primary" :icon="Plus" :disabled="switchOptionFull" @click="addOption(model, index + 1)" />
						<el-button circle type="danger" :icon="Delete" @click="removeOption(model, index)" />
					</div>
				</el-row>
			</template>
		</crud-page>
	</el-dialog>
</template>

<script setup lang="ts">
import { getLabel } from '@/utils/enum'
import { COLUMN_SPAN_TYPES, COMPONENT_TYPES } from '@/constant/enum'
import type {
	CrudField,
	GenTemplateGroupPropertyEntity,
	GenTemplateGroupPropertyQuery,
	SwitchUpdateConfig,
	UseCrudFormOptions,
	UseCrudTableOptions
} from '@/types'
import { useCrud, useSwitchChangeHandler } from '@/hooks'
import CrudPage from '@/components/crud/CrudPage.vue'
import { Delete, Download, Plus, Upload } from '@element-plus/icons-vue'
import { genTemplateGroupPropertyApi } from '@/api'

defineOptions({
	name: 'GenTemplateGroupProperty'
})

const props = defineProps({
	templateGroupId: { type: Number, required: true },
	templateGroupName: { type: String, required: true }
})

const emit = defineEmits(['refresh-data-list'])
const visible = ref(false)

// 统一字段声明
const fields: CrudField[] = [
	{
		key: 'propTitle',
		label: '属性标题',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			tooltip: '属性标题具有唯一性，不能重复',
			ruleList: [{ required: true, message: '属性标题不能为空', trigger: 'blur' }]
		},
		table: {
			width: 120
		}
	},
	{
		key: 'propKey',
		label: '属性键',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			tooltip: '属性键具有唯一性，不能重复',
			ruleList: [{ required: true, message: '属性键不能为空', trigger: 'blur' }]
		}
	},
	{
		key: 'propDefaultValue',
		label: '属性默认值',
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '属性默认值不能为空', trigger: 'blur' }] },
		table: {
			width: 120
		}
	},
	{
		key: 'componentType',
		label: '组件类型',
		component: 'radio',
		options: COMPONENT_TYPES.items,
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '组件类型不能为空', trigger: 'blur' }] },
		table: { formatter: getLabel(COMPONENT_TYPES), minWidth: 120 }
	},
	{
		key: 'required',
		label: '是否必填',
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '是否必填不能为空', trigger: 'blur' }] },
		table: {
			width: 120
		}
	},
	{
		key: 'propOrder',
		label: '排序',
		component: 'input-number',
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '排序不能为空', trigger: 'blur' }] },
		table: { sortable: 'custom', minWidth: 180 }
	},
	{
		key: 'columnSpan',
		label: '布局方式',
		component: 'radio',
		options: COLUMN_SPAN_TYPES.items,
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '布局方式不能为空', trigger: 'blur' }] },
		table: { formatter: getLabel(COLUMN_SPAN_TYPES), width: 120 }
	},
	{ key: 'remark', label: '备注', inForm: true, inTable: true },
	{ key: 'componentOptions', label: '组件选项', inForm: true, form: { ruleList: [{ required: true, message: '组件选项不能为空', trigger: 'blur' }] } }
]

// 表单上下文（computed 自动解包，toValue 取最新值）
const formContext = computed(() => ({ templateGroupId: props.templateGroupId }))

// 表单配置
const emptyLabelData = () => ({ label: '', value: '' })

const formConfig = reactive({
	initFormData: (ctx?: Record<string, any>): GenTemplateGroupPropertyEntity => ({
		id: '',
		templateGroupId: ctx?.templateGroupId ?? '',
		propTitle: '',
		propKey: '',
		propDefaultValue: '',
		componentType: '',
		columnSpan: null,
		componentOptions: [emptyLabelData()],
		required: 0,
		propOrder: 0,
		remark: ''
	}),
	submitApi: genTemplateGroupPropertyApi.submit,
	detailApi: genTemplateGroupPropertyApi.detail,
	beforeDataAssign: (data: any) => {
		if (typeof data.propDefaultValue === 'boolean') {
			data.propDefaultValue = data.propDefaultValue.toString()
		}
		if (Array.isArray(data.componentOptions)) {
			for (const option of data.componentOptions) {
				if (typeof option.value === 'boolean') {
					option.value = option.value.toString()
				}
			}
		}
	},
	labelWidth: '130px',
	subject: '模板组属性',
	formContext
} as UseCrudFormOptions)

// 表格配置
const tableConfig = reactive({
	dataListApi: genTemplateGroupPropertyApi.entityPage,
	deleteApi: genTemplateGroupPropertyApi.deleteList,
	deleteNameKey: 'propTitle',
	exportApi: genTemplateGroupPropertyApi.export,
	importApi: genTemplateGroupPropertyApi.import,
	mountedGetData: false,
	queryContext: computed(() => ({ templateGroupId: props.templateGroupId })),
	initQueryForm: (): GenTemplateGroupPropertyQuery => ({
		templateGroupId: '',
		propTitle: '',
		propKey: ''
	})
} as UseCrudTableOptions)

// 统一 hook
const { tableHook, formHook } = useCrud<GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery, GenTemplateGroupPropertyEntity>({
	subject: '模板组属性',
	fields,
	formConfig,
	tableConfig
})

// 必填开关处理（取消/接口失败时回滚 row.required，不刷新表格，避免 @change 循环触发）
const switchUpdateConfig = {
	switchField: 'required',
	confirmFieldText: '是否必填',
	confirmField: 'propTitle',
	states: [
		{ value: 1, text: '是', isActive: true },
		{ value: 0, text: '否', isActive: false }
	],
	apiFn: (val, row) => genTemplateGroupPropertyApi.changeRequired(row.id, val),
	afterSuccess: () => tableHook.query.getDataList()
} as SwitchUpdateConfig

const { activeValue, inactiveValue, activeText, inactiveText, switchSubmitLoading, switchSubmitHandler } = useSwitchChangeHandler(switchUpdateConfig)

// 排序修改处理
const orderChangeHandle = (cur: number, old: number, row: GenTemplateGroupPropertyEntity) => {
	if (cur === old) return
	genTemplateGroupPropertyApi
		.changeOrder(row.id, cur)
		.then(() => {
			ElMessage.success({ message: '排序修改成功', grouping: true })
			tableHook.query.getDataList()
		})
		.catch(() => {
			row.propOrder = old
		})
}

// 组件选项操作
const hasComponentOptions = computed(() => [2, 3, 4, 5].includes(formHook.form.data.componentType as number))
const switchOptionFull = computed(() => formHook.form.data.componentType === 5 && formHook.form.data.componentOptions.length >= 2)

const addOption = (model: any, index: number) => {
	model.componentOptions.splice(index, 0, emptyLabelData())
}

const removeOption = (model: any, index: number) => {
	model.componentOptions.splice(index, 1)
}

// 组件类型变更时清空选项
watch(
	() => formHook.form.data.componentType,
	(newVal, oldVal) => {
		if (newVal && oldVal && newVal != oldVal && hasComponentOptions.value) {
			formHook.form.data.componentOptions = [emptyLabelData()]
		}
	}
)

// 弹窗初始化
const init = () => {
	visible.value = true
	tableHook.query.getDataList()
}

const closeHandle = () => {
	visible.value = false
	emit('refresh-data-list')
}

defineExpose({ init })
</script>

<style scoped lang="scss">
:deep(.el-switch) {
	--el-switch-on-color: var(--el-color-primary);
	--el-switch-off-color: var(--el-color-danger);
}

.module-actions {
	width: 50px;
	display: flex;
	align-items: center;
	padding-left: 10px;
	gap: 15px;
}
</style>
