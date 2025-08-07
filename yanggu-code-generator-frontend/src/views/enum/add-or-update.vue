<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="dataForm.projectId" clearable :disabled="dataForm.id" placeholder="请选择项目">
					<el-option v-for="item in projectList" :key="item.id" :value="item.id" :label="item.projectName">{{ item.projectName }}</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="枚举名称" prop="enumName">
				<el-input v-model="dataForm.enumName" clearable placeholder="请输入枚举名称"></el-input>
			</el-form-item>
			<el-form-item label="枚举描述" prop="enumDesc">
				<el-input v-model="dataForm.enumDesc" clearable placeholder="请输入枚举描述"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { enumDetailApi, enumSubmitApi } from '@/api/enum'
import { projectEntityListApi } from '@/api/project'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'

const projectList = ref([])
const getProjectList = () => {
	projectEntityListApi({}).then((res: any) => {
		projectList.value = res.data
	})
}

const emit = defineEmits(['refreshDataList'])
const state: FormOptions = reactive({
	submitApi: enumSubmitApi,
	detailApi: enumDetailApi,
	initBefore: getProjectList,
	initFormData: {
		projectId: null,
		enumName: '',
		enumDesc: ''
	},
	emit
})

const dataRules = reactive({
	enumName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	enumDesc: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataForm, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
