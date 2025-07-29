<template>
	<el-dialog v-model="visible" :title="'修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="dataForm.projectId" clearable placeholder="请选择项目" disabled>
					<el-option v-for="item in projectList" :key="item.id" :value="item.id" :label="item.projectName">{{ item.projectName }}</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="数据库名" prop="databaseName">
				<el-input v-model="dataForm.databaseName" placeholder="请输入数据库名"></el-input>
			</el-form-item>
			<el-form-item label="表名" prop="tableName">
				<el-input v-model="dataForm.tableName" placeholder="请输入表名" disabled></el-input>
			</el-form-item>
			<el-form-item label="类名" prop="className">
				<el-input v-model="dataForm.className" placeholder="请输入类名"></el-input>
			</el-form-item>
			<el-form-item label="注释" prop="tableComment">
				<el-input v-model="dataForm.tableComment" placeholder="请输入说明"></el-input>
			</el-form-item>
			<el-form-item label="功能名" prop="functionName">
				<el-input v-model="dataForm.functionName" placeholder="请输入功能名"></el-input>
			</el-form-item>
			<el-form-item label="表单布局" prop="formLayout">
				<el-radio-group v-model="dataForm.formLayout">
					<el-radio v-for="item in FORM_LAYOUT_TYPES" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
				</el-radio-group>
			</el-form-item>
			<el-form-item label="作者" prop="author">
				<el-input v-model="dataForm.author" placeholder="请输入作者"></el-input>
			</el-form-item>
			<el-form-item label="版本" prop="version">
				<el-input v-model="dataForm.version" placeholder="请输入版本"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { tableDetailApi, tableSubmitApi } from '@/api/table'
import { FORM_LAYOUT_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { projectEntityListApi } from '@/api/project'
import { Check, Close } from '@element-plus/icons-vue'

const emit = defineEmits(['refreshDataList'])

const projectList = ref([])

const state: FormOptions = {
	submitApi: tableSubmitApi,
	detailApi: tableDetailApi,
	initBefore: () => {
		projectEntityListApi({}).then(res => {
			projectList.value = res.data
		})
	},
	initFormData: {
		id: null,
		tableName: '',
		databaseName: '',
		className: '',
		tableComment: '',
		projectId: '',
		author: '',
		version: '',
		functionName: '',
		formLayout: ''
	},
	emit
}

const dataRules = reactive({
	databaseName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	className: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	tableComment: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	functionName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	formLayout: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataForm, dataFormRef, init, submitHandle } = useSubmitForm(state)

defineExpose({
	init
})
</script>
