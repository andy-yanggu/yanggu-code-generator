<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false" width="60%">
		<el-form
			ref="dataFormRef"
			:model="state.dataForm"
			:rules="dataRules"
			label-width="130px"
			:validate-on-rule-change="false"
			@keyup.enter="submitHandle()"
		>
			<el-row>
				<el-col :span="12">
					<el-form-item prop="propTitle">
						<template #label>
							<form-label-tooltip label="属性标题" tooltip="属性标题具有唯一性，不能重复"></form-label-tooltip>
						</template>
						<el-input v-model="state.dataForm.propTitle" clearable placeholder="请输入属性标题"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item prop="propKey">
						<template #label>
							<form-label-tooltip label="属性键" tooltip="属性键具有唯一性，不能重复"></form-label-tooltip>
						</template>
						<el-input v-model="state.dataForm.propKey" clearable placeholder="请输入属性键"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="排序" prop="propOrder">
						<el-input-number v-model="state.dataForm.propOrder" :min="0"></el-input-number>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="是否必填" prop="required">
						<el-switch
							v-model="state.dataForm.required"
							:active-value="1"
							:inactive-value="0"
							inline-prompt
							active-text="是"
							inactive-text="否"
						></el-switch>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="布局方式" prop="columnSpan">
						<el-radio-group v-model="state.dataForm.columnSpan" :options="COLUMN_SPAN_TYPES"></el-radio-group>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="备注" prop="remark">
						<el-input v-model="state.dataForm.remark" clearable placeholder="请输入备注"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-form-item label="属性默认值" prop="propDefaultValue">
				<el-input v-model="state.dataForm.propDefaultValue" clearable placeholder="请输入属性默认值"></el-input>
			</el-form-item>
			<el-form-item label="组件类型" prop="componentType">
				<el-radio-group v-model="state.dataForm.componentType" :options="COMPONENT_TYPES"></el-radio-group>
			</el-form-item>
			<el-form-item v-if="hasComponentOptions" label="组件选项" prop="componentOptions">
				<el-row
					v-for="(item, index) in state.dataForm.componentOptions"
					:key="index"
					:gutter="10"
					:style="{ marginBottom: index < state.dataForm.componentOptions.length - 1 ? '18px' : '0' }"
				>
					<el-col :span="10">
						<el-form-item :prop="`componentOptions[${index}].label`">
							<el-input v-model="item.label" clearable placeholder="请输入选项标题" style="width: 220px"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="10">
						<el-form-item :prop="`componentOptions[${index}].value`">
							<el-input v-model="item.value" clearable placeholder="请输入选项值" style="width: 220px"></el-input>
						</el-form-item>
					</el-col>
					<div class="module-actions">
						<el-button
							circle
							type="primary"
							:icon="Plus"
							:disabled="switchOptionFull"
							@click="() => state.dataForm.componentOptions.splice(index + 1, 0, emptyLabelData())"
						></el-button>
						<el-button circle type="danger" :icon="Delete" @click="() => state.dataForm.componentOptions.splice(index, 1)"></el-button>
					</div>
				</el-row>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { useSubmitForm } from '@/hooks'
import { Check, Close, Delete, Plus } from '@element-plus/icons-vue'
import { COLUMN_SPAN_TYPES, COMPONENT_TYPES } from '@/constant/enum'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import { genTemplateGroupPropertyApi } from '@/api'
import { FormOptions, GenTemplateGroupPropertyEntity, LabelData } from '@/types'
import { FormItemRule } from 'element-plus'

defineOptions({
	name: 'GenTemplateGroupPropertyForm'
})

const emit = defineEmits(['refreshDataList'])

const emptyLabelData = (): LabelData => ({ label: '', value: '' })

// 初始化表单数据
const initFormData = (ctx?: Record<string, any>): GenTemplateGroupPropertyEntity => ({
	id: '',
	templateGroupId: ctx?.templateGroupId ?? '',
	propTitle: '',
	propKey: '',
	propDefaultValue: '',
	componentType: '',
	columnSpan: null,
	componentOptions: [emptyLabelData()] as LabelData[],
	required: 0,
	propOrder: 0,
	remark: ''
})

const state = reactive({
	// 提交API
	submitApi: genTemplateGroupPropertyApi.submit,
	// 详情API
	detailApi: genTemplateGroupPropertyApi.detail,
	// 初始化表单数据
	initFormData,
	dataForm: initFormData(),
	emit
} as FormOptions<GenTemplateGroupPropertyEntity>)

watch(
	() => state.dataForm.componentType,
	(newValue, oldValue) => {
		if (newValue && oldValue && newValue != oldValue && hasComponentOptions.value) {
			state.dataForm.componentOptions = [emptyLabelData()]
		}
	}
)

// 是否需要组件选项
const hasComponentOptions = computed(() => [2, 3, 4, 5].includes(state.dataForm.componentType as number))

// 开关类型只能有两个选项
const switchOptionFull = computed(() => state.dataForm.componentType === 5 && state.dataForm.componentOptions.length === 2)

const dataRules = computed(() => {
	const rules: Record<string, FormItemRule[]> = {
		propTitle: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		propKey: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		required: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		propDefaultValue: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		componentType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		propOrder: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		columnSpan: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		componentOptions: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
	}

	// 动态模块校验
	state.dataForm.componentOptions.forEach((_, index) => {
		rules[`componentOptions[${index}].label`] = [{ required: true, message: '选项标题不能为空', trigger: 'blur' }]
		rules[`componentOptions[${index}].value`] = [{ required: true, message: '选项值不能为空', trigger: 'blur' }]
	})
	return rules
})

const { visible, dataFormRef, init, submitHandle, submitLoading, dialogTitle } = useSubmitForm(state)

const initHandle = (templateGroupId: number, id?: number) => {
	init(id, undefined, { templateGroupId })
}

defineExpose({
	initHandle
})
</script>
<style lang="scss" scoped>
.module-actions {
	width: 50px; /* ⭐ 固定宽度，解决大屏抖动问题 */
	display: flex;
	align-items: center;
	padding-left: 10px;
	gap: 15px;
}
</style>
