<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="字段类型" prop="columnType">
				<el-input v-model="state.dataForm.columnType" clearable placeholder="请输入字段类型"></el-input>
			</el-form-item>
			<el-form-item label="属性类型" prop="attrType">
				<el-select v-model="state.dataForm.attrType" clearable placeholder="请选择属性类型">
					<el-option v-for="item in ATTR_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="包名" prop="packageName">
				<el-input v-model="state.dataForm.packageName" clearable placeholder="请输入包名"></el-input>
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
import { fieldTypeDetailApi, fieldTypeSubmitApi } from '@/api/field-type'
import { ATTR_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
	submitApi: fieldTypeSubmitApi,
	detailApi: fieldTypeDetailApi,
	dataForm: {
		id: '',
		columnType: '',
		attrType: '',
		packageName: ''
	},
	emit
})

const dataRules = reactive({
	columnType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	attrType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
