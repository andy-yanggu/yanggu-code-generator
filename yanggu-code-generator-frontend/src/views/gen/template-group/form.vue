<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="120px" @keyup.enter="submitHandle()">
			<el-form-item prop="groupName">
				<template #label>
					<form-label-tooltip label="模板组名称" tooltip="模板组名称具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.groupName" clearable placeholder="请输入模板组名称"></el-input>
			</el-form-item>
			<el-form-item label="模板组类型" prop="type">
				<el-select
					v-model="state.dataForm.type"
					:options="TEMPLATE_GROUP_TYPES"
					:disabled="state.dataForm.id != null"
					clearable
					filterable
					placeholder="请选择模板组类型"
				></el-select>
			</el-form-item>
			<el-form-item label="模板组描述" prop="groupDesc">
				<el-input v-model="state.dataForm.groupDesc" clearable placeholder="请输入模板组描述"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import { genTemplateGroupApi } from '@/api'
import { FormOptions, GenTemplateGroupEntity } from '@/types'

defineOptions({
	name: 'GenTemplateGroupForm'
})

// 初始化表单数据
const initFormData = (): GenTemplateGroupEntity => ({
	id: null,
	groupName: '',
	type: '',
	groupDesc: ''
})

const emit = defineEmits(['refreshDataList'])

const state = reactive({
	// 提交API
	submitApi: genTemplateGroupApi.submit,
	// 详情API
	detailApi: genTemplateGroupApi.detail,
	initFormData,
	// 表单数据
	dataForm: initFormData(),
	initAfter: () => {
		if (formType.value == 'copy') {
			state.submitApi = genTemplateGroupApi.copy
			state.message = '模板组和下的所有模板、属性已复制'
			state.duration = 2000
			state.dataForm.groupName = state.dataForm.groupName + '_复制'
		} else {
			state.submitApi = genTemplateGroupApi.submit
			state.message = ''
			state.duration = 500
		}
	},
	emit
} as FormOptions<GenTemplateGroupEntity>)

const dataRules = reactive({
	groupName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	type: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, formType, dialogTitle, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
