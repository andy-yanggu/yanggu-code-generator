<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="字段类型" prop="columnType">
				<el-input v-model="dataForm.columnType" placeholder="请输入字段类型"></el-input>
			</el-form-item>
			<el-form-item label="属性类型" prop="attrType">
				<el-select v-model="dataForm.attrType" clearable placeholder="请选择属性类型">
					<el-option v-for="item in ATTR_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="属性包名" prop="packageName">
				<el-input v-model="dataForm.packageName" placeholder="请输入属性包名"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { fieldTypeDetailApi, fieldTypeSubmitApi } from '@/api/field-type'
import { ATTR_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
	submitApi: fieldTypeSubmitApi,
	detailApi: fieldTypeDetailApi,
	initFormData: {
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

const { visible, dataForm, dataFormRef, init, submitHandle } = useSubmitForm(state)

defineExpose({
	init
})
</script>
