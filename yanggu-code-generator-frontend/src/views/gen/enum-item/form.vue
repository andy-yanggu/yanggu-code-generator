<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="120px" @keyup.enter="submitHandle()">
			<el-form-item prop="enumItemName">
				<template #label>
					<form-label-tooltip label="枚举项名称" tooltip="使用英文大写字母，单词之间使用'_'拼接；该字段具有唯一性"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.enumItemName" clearable placeholder="请输入枚举项名称"></el-input>
			</el-form-item>
			<el-form-item prop="enumItemCode">
				<template #label>
					<form-label-tooltip label="枚举项编码" tooltip="枚举项编码具有唯一性"></form-label-tooltip>
				</template>
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
import { genEnumItemApi } from '@/api'
import { FormOptions, GenEnumItemEntity } from '@/types'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'GenEnumItemForm'
})

// 初始化表单数据
const initFormData = (ctx?: Record<string, any>): GenEnumItemEntity => ({
	id: '',
	enumId: ctx?.enumId ?? -1,
	enumItemName: '',
	enumItemCode: '',
	enumItemDesc: '',
	enumItemOrder: 0
})

const emit = defineEmits(['refreshDataList'])

const state = reactive({
	// 提交API
	submitApi: genEnumItemApi.submit,
	// 详情API
	detailApi: genEnumItemApi.detail,
	initFormData,
	dataForm: initFormData(),
	emit
} as FormOptions<GenEnumItemEntity>)

const dataRules = reactive({
	enumItemName: [{ required: true, message: '枚举项名称不能为空', trigger: 'blur' }],
	enumItemCode: [{ required: true, message: '枚举项编码不能为空', trigger: 'blur' }],
	enumItemDesc: [{ required: true, message: '枚举项描述不能为空', trigger: 'blur' }],
	enumItemOrder: [{ required: true, message: '枚举项排序不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading, dialogTitle } = useSubmitForm(state)

defineExpose({
	init
})
</script>
