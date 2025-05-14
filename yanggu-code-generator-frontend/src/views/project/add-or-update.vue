<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="项目名称" prop="projectName">
				<el-input v-model="dataForm.projectName" placeholder="请输入项目名称"></el-input>
			</el-form-item>
			<el-form-item label="项目包名" prop="projectPackage">
				<el-input v-model="dataForm.projectPackage" placeholder="请输入项目包名"></el-input>
			</el-form-item>
			<el-form-item label="项目版本" prop="projectVersion">
				<el-input v-model="dataForm.projectVersion" placeholder="请输入项目版本"></el-input>
			</el-form-item>
			<el-form-item label="数据源" prop="datasourceId">
				<el-select v-model="dataForm.datasourceId" clearable filterable placeholder="请选择数据源" style="width: 100%">
					<el-option v-for="item in datasourceList" :key="item.id" :label="item.connName" :value="item.id">
						<span style="font-weight: bold">{{ item.connName }}</span>
						<span v-if="item.dataSourceDesc && item.dataSourceDesc.trim()" style="color: #999; font-size: 12px">（{{ item.dataSourceDesc }}） </span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="项目模板组" prop="projectTemplateGroupId">
				<el-select v-model="dataForm.projectTemplateGroupId" placeholder="请选择项目模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in projectTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="表模板组" prop="tableTemplateGroupId">
				<el-select v-model="dataForm.tableTemplateGroupId" placeholder="请选择表模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in tableTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="生成方式" prop="generatorType">
				<el-radio-group v-model="dataForm.generatorType">
					<el-radio v-for="item in PROJECT_GENERATE_TYPES" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
				</el-radio-group>
			</el-form-item>
			<el-form-item prop="baseClassId" label="Entity基类">
				<el-select v-model="dataForm.baseClassId" placeholder="请选择Entity基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id" :label="item.code" :value="item.id">
						<span style="font-weight: bold">{{ item.code }}</span>
						<span v-if="item.remark && item.remark.trim()" style="color: #999; font-size: 12px">（{{ item.remark }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="后端路径" prop="backendPath">
				<el-input v-model="dataForm.backendPath" placeholder="请输入后端路径"></el-input>
			</el-form-item>
			<el-form-item label="前端路径" prop="frontendPath">
				<el-input v-model="dataForm.frontendPath" placeholder="请输入前端路径"></el-input>
			</el-form-item>
			<el-form-item label="作者" prop="author">
				<el-input v-model="dataForm.author" placeholder="请输入作者"></el-input>
			</el-form-item>
			<el-form-item label="项目描述" prop="projectDesc">
				<el-input v-model="dataForm.projectDesc" placeholder="请输入项目描述"></el-input>
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
import { datasourceEntityListApi } from '@/api/datasource'
import { templateGroupEntityListApi } from '@/api/templateGroup'
import { baseClassEntityListApi } from '@/api/baseClass'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: null,
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
	generatorType: null
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

	//获取下拉数据
	getList()
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

const datasourceList = ref([])
const projectTemplateGroupList = ref([])
const tableTemplateGroupList = ref([])
const baseClassList = ref([])

const getList = () => {
	//数据源下拉
	datasourceEntityListApi({}).then(res => {
		datasourceList.value = res.data
	})

	//模板组下拉
	templateGroupEntityListApi({}).then(res => {
		projectTemplateGroupList.value = res.data.filter(item => item.type === 0)
		tableTemplateGroupList.value = res.data.filter(item => item.type === 1)
	})

	//基类下拉
	baseClassEntityListApi({}).then(res => {
		baseClassList.value = res.data
	})
}

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
