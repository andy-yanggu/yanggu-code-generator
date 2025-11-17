<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="130px" @keyup.enter="submitHandle()">
			<el-form-item prop="propTitle">
				<template #label>
					<form-label-tooltip label="属性标题" tooltip="属性标题具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.propTitle" clearable placeholder="请输入属性标题"></el-input>
			</el-form-item>
			<el-form-item prop="propKey">
				<template #label>
					<form-label-tooltip label="属性键" tooltip="属性键具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.propKey" clearable placeholder="请输入属性键"></el-input>
			</el-form-item>
			<el-form-item label="组件类型" prop="componentType">
				<el-radio-group v-model="state.dataForm.componentType">
					<el-radio v-for="item in COMPONENT_TYPES" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
				</el-radio-group>
			</el-form-item>
			<el-form-item v-if="hasComponentOptions" label="组件选项" prop="componentOptions">
				<el-row
					v-for="(item, index) in state.dataForm.componentOptions"
					:key="item.value"
					:gutter="10"
					:style="{ marginBottom: index < state.dataForm.componentOptions.length - 1 ? '10px' : '0' }"
				>
					<el-col :span="11">
						<el-input v-model="item.label" clearable placeholder="请输入选项标题"></el-input>
					</el-col>
					<el-col :span="11">
						<el-input v-model="item.value" clearable placeholder="请输入选项值"></el-input>
					</el-col>
					<el-col :span="2">
						<div style="height: 100%; width: 100%; display: flex; align-items: center; justify-content: flex-end; gap: 12px; padding-left: 10px">
							<el-icon style="cursor: pointer" @click="() => state.dataForm.componentOptions.splice(index + 1, 0, { label: '', value: '' })">
								<Plus></Plus>
							</el-icon>
							<el-icon
								v-if="state.dataForm.componentOptions.length > 1"
								style="cursor: pointer"
								@click="() => state.dataForm.componentOptions.splice(index, 1)"
							>
								<Delete></Delete>
							</el-icon>
						</div>
					</el-col>
				</el-row>
			</el-form-item>
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
			<el-form-item label="属性默认值" prop="propDefaultValue">
				<el-input v-model="state.dataForm.propDefaultValue" clearable placeholder="请输入属性默认值"></el-input>
			</el-form-item>
			<el-form-item label="排序" prop="propOrder">
				<el-input-number v-model="state.dataForm.propOrder" :min="0"></el-input-number>
			</el-form-item>
			<el-form-item label="备注" prop="remark">
				<el-input v-model="state.dataForm.remark" clearable placeholder="请输入备注"></el-input>
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
import { genTemplateGroupPropertyApi, GenTemplateGroupPropertyEntity } from '@/api/gen/template-group-property'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close, Delete, Plus } from '@element-plus/icons-vue'
import { COMPONENT_TYPES } from '@/constant/enum'
import { LabelData } from '@/types/common'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

const emit = defineEmits(['refreshDataList'])

const state = reactive({
	// 提交API
	submitApi: genTemplateGroupPropertyApi.submit,
	// 详情API
	detailApi: genTemplateGroupPropertyApi.detail,
	// 表单数据
	dataForm: {
		id: '',
		templateGroupId: '',
		propTitle: '',
		propKey: '',
		propDefaultValue: '',
		componentType: '',
		componentOptions: [{ label: '', value: '' }] as LabelData[],
		required: 0,
		propOrder: 0,
		remark: ''
	},
	submitBefore: () => {
		if (!hasComponentOptions.value) {
			state.dataForm.componentOptions = []
		}
	},
	emit
} as FormOptions<GenTemplateGroupPropertyEntity>)

watch(
	() => state.dataForm.componentType,
	() => {
		if (hasComponentOptions.value && state.dataForm.componentOptions.length === 0) {
			state.dataForm.componentOptions.push({ label: '', value: '' })
		}
	}
)

const hasComponentOptions = computed(() => [2, 3, 4, 5].includes(state.dataForm.componentType! as number))

const componentOptions = (_: any, __: any, callback: any) => {
	if (!hasComponentOptions.value) {
		callback()
		return
	}
	const message = '必填项不能为空'
	if (state.dataForm.componentOptions.length === 0) {
		callback(new Error(message))
		return
	}
	for (let i = 0; i < state.dataForm.componentOptions.length; i++) {
		const item = state.dataForm.componentOptions[i]
		if (!item.label || !item.value) {
			callback(new Error(`第${i + 1}个选项标题或选项值不能为空`))
			return
		}
	}
	callback()
}

const dataRules = reactive({
	propTitle: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	propKey: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	required: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	componentType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	propOrder: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	componentOptions: [{ required: true, validator: componentOptions, trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

const initHandle = (templateGroupId: number, id?: number) => {
	state.dataForm.templateGroupId = templateGroupId
	init(id)
}

defineExpose({
	initHandle
})
</script>
