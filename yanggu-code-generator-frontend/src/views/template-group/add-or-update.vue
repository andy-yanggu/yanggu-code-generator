<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="模板组名称" prop="groupName">
				<el-input v-model="state.dataForm.groupName" clearable placeholder="请输入模板组名称"></el-input>
			</el-form-item>
			<el-form-item label="模板组类型" prop="type">
				<el-select v-model="state.dataForm.type" :disabled="state.dataForm.id" clearable filterable placeholder="请选择模板组类型">
					<el-option v-for="item in TEMPLATE_GROUP_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
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
import { templateGroupDetailApi, templateGroupSubmitApi } from '@/api/template-group'
import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
	// 提交API
	submitApi: templateGroupSubmitApi,
	// 详情API
	detailApi: templateGroupDetailApi,
	// 表单数据
	dataForm: {
		id: '',
		groupName: '',
		type: '',
		groupDesc: ''
	},
	emit
})

const dataRules = reactive({
	groupName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	type: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
