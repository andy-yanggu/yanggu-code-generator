<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item prop="columnType">
				<template #label>
					<form-label-tooltip label="字段类型" tooltip="数据库中的字段类型；字段类型具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.columnType" clearable placeholder="请输入字段类型"></el-input>
			</el-form-item>
			<el-form-item label="属性类型" prop="attrType">
				<el-select v-model="state.dataForm.attrType" :options="ATTR_TYPES" clearable filterable placeholder="请选择属性类型"></el-select>
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
import { genFieldTypeApi } from '@/api'
import { FormOptions, GenFieldTypeEntity } from '@/types'
import { ATTR_TYPES } from '@/constant/enum'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'GenFieldTypeForm'
})

const initFormData = (): GenFieldTypeEntity => ({
	id: -1,
	columnType: '',
	attrType: '',
	packageName: ''
})

const emit = defineEmits(['refreshDataList'])

const state = reactive({
	submitApi: genFieldTypeApi.submit,
	detailApi: genFieldTypeApi.detail,
	initFormData,
	emit
} as FormOptions<GenFieldTypeEntity>)

const dataRules = reactive({
	columnType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	attrType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, dialogTitle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
