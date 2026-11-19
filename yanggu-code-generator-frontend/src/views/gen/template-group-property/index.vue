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
						val => {
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
					@change="(cur, old) => orderChangeHandle(cur, old, row)"
				></el-input-number>
			</template>

			<!-- 表单字段 required：number ↔ boolean 转换 -->
			<template #form-required="{ model }">
				<el-switch
					:model-value="model.required === 1"
					active-text="是"
					inactive-text="否"
					inline-prompt
					@update:model-value="val => (model.required = val ? 1 : 0)"
				/>
			</template>

			<!-- 表单字段 componentOptions：动态选项列表 -->
			<template #form-componentOptions="{ model }">
				<el-row
					v-for="(item, index) in componentOptionsOf(model)"
					:key="index"
					class="component-option-row"
					:gutter="10"
					:style="{ marginBottom: index < componentOptionsOf(model).length - 1 ? '18px' : '0' }"
				>
					<el-col :span="10">
						<el-form-item :prop="`componentOptions[${index}].label`">
							<el-input v-model="item.label" clearable placeholder="请输入选项标题" style="width: 100%" />
						</el-form-item>
					</el-col>
					<el-col :span="10">
						<el-form-item :prop="`componentOptions[${index}].value`">
							<el-input v-model="item.value" clearable placeholder="请输入选项值" style="width: 100%" />
						</el-form-item>
					</el-col>
					<div class="module-actions">
						<el-space size="default">
							<el-button circle :icon="Top" :disabled="index === 0" title="上移" @click="moveOption(model, index, -1)" />
							<el-button circle :icon="Bottom" :disabled="index === componentOptionsOf(model).length - 1" title="下移" @click="moveOption(model, index, 1)" />
							<el-button v-show="componentOptionsOf(model).length > 1" type="danger" circle :icon="Delete" title="删除" @click="removeOption(model, index)" />
						</el-space>
					</div>
				</el-row>
				<el-row class="add-option-row" :gutter="10">
					<el-button class="add-option-btn" type="primary" plain :icon="Plus" @click="addOption(model, componentOptionsOf(model).length)">新增选项</el-button>
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
import { Bottom, Delete, Download, Plus, Top, Upload } from '@element-plus/icons-vue'
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
		tooltip: '属性标题具有唯一性，不能重复',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			ruleList: [{ required: true, message: '属性标题不能为空', trigger: 'blur' }]
		},
		table: {
			width: 120
		}
	},
	{
		key: 'propKey',
		label: '属性键',
		tooltip: '属性键具有唯一性，不能重复',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			ruleList: [{ required: true, message: '属性键不能为空', trigger: 'blur' }]
		},
		table: {
			width: 120
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
	dialogWidth: '65%', // 表单弹窗加宽，容纳操作区三枚按钮，与 project 表单观感一致
	subject: '模板组属性',
	formContext
} as UseCrudFormOptions)

// 表格配置
const tableConfig = reactive({
	dataListApi: genTemplateGroupPropertyApi.page,
	deleteApi: genTemplateGroupPropertyApi.deleteList,
	deleteNameKey: 'propTitle',
	exportApi: genTemplateGroupPropertyApi.export,
	importApi: genTemplateGroupPropertyApi.import,
	mountedGetData: false,
	queryContext: computed(() => ({ templateGroupId: props.templateGroupId })),
	initQueryData: (): GenTemplateGroupPropertyQuery => ({
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
// el-input-number 的 change 契约是 (cur: number | undefined, prev: number | undefined) => any：
// 输入框被清空时 emit 的是 undefined。形参写窄成 number 会触发逆变检查报 TS2322，
// 所以按契约接收，并在开头把 undefined 挡掉——清空不应触发排序修改。
// （cur / old 是从不被重新赋值的形参，收窄后在 .catch 回调里仍然是 number）
const orderChangeHandle = (cur: number | undefined, old: number | undefined, row: GenTemplateGroupPropertyEntity) => {
	if (cur === undefined || old === undefined || cur === old) return
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
/** 组件选项的形状（与 emptyLabelData 一致） */
interface ComponentOption {
	label: string
	value: string
}

/**
 * 把插槽作用域里的 model.componentOptions 收窄成数组。
 *
 * 插槽传出的 model 是 Record<string, any>，直接 v-for 遍历 model.componentOptions 时
 * vue-tsc 无法判定它是数组还是对象，会把 index 推成 string | number（对象遍历时的 key），
 * 导致 index < ... / index + 1 / removeOption(model, index) 全部报错。
 * 收窄成数组后 index 才是 number；返回的是同一个响应式数组引用，v-model 仍能写回。
 */
const componentOptionsOf = (model: any): ComponentOption[] => (model?.componentOptions ?? []) as ComponentOption[]

const hasComponentOptions = computed(() => [2, 3, 4, 5].includes(formHook.form.data.componentType as number))

const addOption = (model: any, index: number) => {
	model.componentOptions.splice(index, 0, emptyLabelData())
}

const removeOption = (model: any, index: number) => {
	model.componentOptions.splice(index, 1)
}

// 选项上下移：与相邻项交换位置（越界防御式直接返回，按钮 disabled 已挡常规场景）
const moveOption = (model: any, index: number, dir: -1 | 1) => {
	const list = model.componentOptions as ComponentOption[]
	const target = index + dir
	if (target < 0 || target >= list.length) return
	const from = list[index]
	list[index] = list[target]
	list[target] = from
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
	width: 130px; /* 上移/下移/删除三枚圆钮(el-space间距12×2)+左内边距10 ≈ 130px，与 project 模块行一致，固定宽避免显隐抖动 */
	display: flex;
	align-items: center;
	padding-left: 10px;
}

/* 组件选项行：占满 el-form-item__content 整行，两列 flex 均分填满，操作区固定宽靠右 */
.component-option-row {
	width: 100%;

	:deep(.el-col) {
		flex: 1 1 0;
		max-width: none;
	}
}

/* 新增选项：外层 el-row 是 el-form-item__content(flex) 的子项，默认缩到内容宽度，需占满整行 */
.add-option-row {
	width: 100%;
}

/* 新增选项：整行虚线占位添加区，图标+文字居中 */
.add-option-btn {
	width: 100%; /* 撑满选项整行宽度 */
	margin-top: 20px;
	border-style: dashed; /* 虚线占位语义 */

	span {
		justify-content: center; /* 图标+文字水平居中 */
	}
}
</style>
