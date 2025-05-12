<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="项目名" prop="projectName">
				<el-input v-model="dataForm.projectName" placeholder="项目名"></el-input>
			</el-form-item>
			<el-form-item label="项目包名" prop="projectPackage">
				<el-input v-model="dataForm.projectPackage" placeholder="项目包名"></el-input>
			</el-form-item>
			<el-form-item label="项目版本" prop="projectVersion">
				<el-input v-model="dataForm.projectVersion" placeholder="项目版本"></el-input>
			</el-form-item>
			<el-form-item label="数据源ID" prop="datasourceId">
				<el-select v-model="dataForm.datasourceId" placeholder="请选择">
					<el-option label="请选择" value="0"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="项目模板组ID" prop="projectTemplateGroupId">
				<el-select v-model="dataForm.projectTemplateGroupId" placeholder="请选择">
					<el-option label="请选择" value="0"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="表模板组ID" prop="tableTemplateGroupId">
				<el-select v-model="dataForm.tableTemplateGroupId" placeholder="请选择">
					<el-option label="请选择" value="0"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="后端路径" prop="backendPath">
				<el-input v-model="dataForm.backendPath" placeholder="后端路径"></el-input>
			</el-form-item>
			<el-form-item label="前端路径" prop="frontendPath">
				<el-input v-model="dataForm.frontendPath" placeholder="前端路径"></el-input>
			</el-form-item>
			<el-form-item label="项目描述" prop="projectDesc">
				<el-input v-model="dataForm.projectDesc" placeholder="项目描述"></el-input>
			</el-form-item>
			<el-form-item label="作者" prop="author">
				<el-input v-model="dataForm.author" placeholder="作者"></el-input>
			</el-form-item>
			<el-form-item label="基类ID" prop="baseClassId">
				<el-select v-model="dataForm.baseClassId" placeholder="请选择">
					<el-option label="请选择" value="0"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="生成方式（0-zip压缩包，1-服务器本地）" prop="generatorType">
				<el-radio-group v-model="dataForm.generatorType">
					<el-radio :label="0">启用</el-radio>
					<el-radio :label="1">禁用</el-radio>
				</el-radio-group>
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
import { projectDetailApi, projectSubmitApi } from '@/api/project'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	projectName: '',
	projectPackage: '',
	projectVersion: '',
	datasourceId: '',
	projectTemplateGroupId: '',
	tableTemplateGroupId: '',
	backendPath: '',
	frontendPath: '',
	projectDesc: '',
	author: '',
	baseClassId: '',
	generatorType: '',
	createTime: '',
	updateTime: '',
	isDelete: ''
})

const init = (id?: number) => {
	visible.value = true
	dataForm.id = null

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	if (id) {
		getProject(id)
	}
}

const getProject = (id: number) => {
	projectDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = ref({
	projectName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectPackage: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectVersion: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	datasourceId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectTemplateGroupId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	tableTemplateGroupId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		projectSubmitApi(dataForm).then(() => {
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
