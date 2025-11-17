<template>
	<el-dialog v-model="visible" :title="dialogTitle(mode)" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="130px" @keyup.enter="submitHandle()">
			<el-form-item prop="baseClassName">
				<template #label>
					<form-label-tooltip label="基类名称" tooltip="基类名称具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.baseClassName" clearable placeholder="请输入基类名称"></el-input>
			</el-form-item>
			<el-form-item prop="fullClassName">
				<template #label>
					<form-label-tooltip label="基类全类名" tooltip="基类全类名具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<div style="display: flex; align-items: center; gap: 10px; width: 100%">
					<el-input v-model="state.dataForm.packageName" style="flex: 1" placeholder="请输入基类包名"></el-input>
					<span>.</span>
					<el-input v-model="state.dataForm.className" style="flex: 1" placeholder="请输入基类类名"></el-input>
				</div>
			</el-form-item>
			<el-form-item prop="fields">
				<template #label>
					<form-label-tooltip label="基类字段" tooltip="多个字段使用英文逗号拼接"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.fields" clearable placeholder="请输入基类字段，多个用英文逗号分隔"></el-input>
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
import { PropType, reactive } from 'vue'
import { genBaseClassApi, GenBaseClassEntity } from '@/api/gen/base-class'
import { FormOptions, FormType, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'GenBaseClassForm'
})

// 定义组件props
const props = defineProps({
	mode: {
		type: String as PropType<FormType>,
		required: true
	}
})

const emit = defineEmits(['refreshDataList'])
const state = reactive({
	// 提交API
	submitApi: genBaseClassApi.submit,
	// 详情API
	detailApi: genBaseClassApi.detail,
	// 表单数据
	dataForm: {
		id: '',
		baseClassName: '',
		packageName: '',
		className: '',
		fields: '',
		remark: ''
	},
	initBefore: () => {
		state.dataForm.packageName = ''
		state.dataForm.className = ''
	},
	initAfter: () => {
		if (props.mode === 'copy') {
			state.dataForm.baseClassName = state.dataForm.baseClassName + '_复制'
			state.dataForm.id = ''
			state.message = '复制成功'
		} else {
			state.message = ''
		}
	},
	emit
} as FormOptions<GenBaseClassEntity>)

const dataRules = reactive({
	baseClassName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	fullClassName: [{ required: true, validator: (_: any, __: any, callback: any) => callback(), message: '必填项不能为空', trigger: 'blur' }],
	packageName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	className: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	fields: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, dialogTitle, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
