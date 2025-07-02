<template>
	<el-dialog v-model="visible" :title="'修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="数据库名" prop="databaseName">
				<el-input v-model="dataForm.databaseName" placeholder="请输入数据库名" disabled></el-input>
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
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { tableDetailApi, tableSubmitApi } from '@/api/table'
import { projectEntityListApi } from '@/api/project'
import { FORM_LAYOUT_TYPES } from '@/constant/enum'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()
const projectList = ref([])

const dataForm = reactive({
	id: null,
	tableName: '',
	databaseName: '',
	className: '',
	tableComment: '',
	projectId: '',
	author: '',
	version: '',
	functionName: '',
	formLayout: '',
	createTime: '',
	updateTime: '',
	isDelete: ''
})

const getProjectList = () => {
	projectEntityListApi({}).then(res => {
		projectList.value = res.data
	})
}

const init = (id?: number) => {
	visible.value = true
	dataForm.id = null

	getProjectList()

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	if (id) {
		getTable(id)
	}
}

const dataRules = reactive({
	className: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	tableComment: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	functionName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	formLayout: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const getTable = (id: number) => {
	tableDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		tableSubmitApi(dataForm).then(() => {
			ElMessage.success({
				message: '操作成功',
				duration: 500,
				onClose: () => {
					visible.value = false
					emit('refreshDataList')
				}
			})
		})
	})
}

defineExpose({
	init
})
</script>
