<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="基类包名" prop="packageName">
				<el-input v-model="state.dataForm.packageName" clearable placeholder="请输入基类包名"></el-input>
			</el-form-item>
			<el-form-item label="基类类名" prop="className">
				<el-input v-model="state.dataForm.className" clearable placeholder="请输入基类类名"></el-input>
			</el-form-item>
			<el-form-item label="基类字段" prop="fields">
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
import { reactive } from 'vue'
import { baseClassDetailApi, baseClassSubmitApi } from '@/api/base-class'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'

const emit = defineEmits(['refreshDataList'])
const state: FormOptions = reactive({
	// 提交API
	submitApi: baseClassSubmitApi,
	// 详情API
	detailApi: baseClassDetailApi,
	// 表单数据
	initFormData: {
		id: null,
		packageName: '',
		className: '',
		fields: '',
		remark: ''
	},
	emit
})

const dataRules = reactive({
	packageName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	className: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	fields: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible,, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
