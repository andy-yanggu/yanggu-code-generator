<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="枚举项名称" prop="enumItemName">
				<el-input v-model="state.dataForm.enumItemName" clearable placeholder="请输入枚举项名称"></el-input>
			</el-form-item>
			<el-form-item label="枚举项编码" prop="enumItemCode">
				<el-input v-model="state.dataForm.enumItemCode" clearable placeholder="请输入枚举项编码"></el-input>
			</el-form-item>
			<el-form-item label="枚举项描述" prop="enumItemDesc">
				<el-input v-model="state.dataForm.enumItemDesc" clearable placeholder="请输入枚举项描述"></el-input>
			</el-form-item>
			<el-form-item label="枚举项排序" prop="enumItemOrder">
				<el-input-number v-model="state.dataForm.enumItemOrder" :min="0" size="small"></el-input-number>
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
import { enumItemDetailApi, enumItemSubmitApi } from '@/api/enum-item'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
	// 提交API
	submitApi: enumItemSubmitApi,
	// 详情API
	detailApi: enumItemDetailApi,
	// 详情数据
	initFormData: {
		id: null,
		enumId: -1,
		enumItemName: '',
		enumItemCode: '',
		enumItemDesc: '',
		enumItemOrder: 0
	},
	emit
})

const dataRules = reactive({
	enumItemName: [{ required: true, message: '请输入枚举项名称', trigger: 'blur' }],
	enumItemCode: [{ required: true, message: '请输入枚举项编码', trigger: 'blur' }],
	enumItemDesc: [{ required: true, message: '请输入枚举项描述', trigger: 'blur' }],
	enumItemOrder: [{ required: true, message: '请配置枚举项排序', trigger: 'blur' }]
})

const { visible,, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

const initData = (enumId: number, id?: number) => {
	dataForm.enumId = enumId
	init(id)
}

defineExpose({
	initData
})
</script>
