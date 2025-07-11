<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="项目名称" prop="projectName">
				<el-input v-model="dataForm.projectName" placeholder="使用英文小写字母，单词之间使用'-'拼接"></el-input>
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
						<span v-if="item.datasourceDesc && item.datasourceDesc.trim()" style="color: #999; font-size: 12px">（{{ item.datasourceDesc }}） </span>
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
			<el-form-item label="枚举模板组" prop="enumTemplateGroupId">
				<el-select v-model="dataForm.enumTemplateGroupId" placeholder="请选择枚举模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in enumTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
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
			<el-form-item prop="entityBaseClassId" label="Entity基类">
				<el-select v-model="dataForm.entityBaseClassId" placeholder="请选择Entity基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id" :label="`${item.packageName}.${item.className}`" :value="item.id">
						<span style="font-weight: bold">{{ item.packageName }}.{{ item.className }}</span>
						<span v-if="item.remark && item.remark.trim()" style="color: #999; font-size: 12px">（{{ item.remark }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item prop="voBaseClassId" label="VO基类">
				<el-select v-model="dataForm.voBaseClassId" placeholder="请选择VO基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id" :label="`${item.packageName}.${item.className}`" :value="item.id">
						<span style="font-weight: bold">{{ item.packageName }}.{{ item.className }}</span>
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
import { projectDetailApi, projectSubmitApi } from '@/api/project'
import { datasourceEntityListApi } from '@/api/datasource'
import { templateGroupEntityListApi } from '@/api/template-group'
import { baseClassEntityListApi } from '@/api/base-class'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'

const emit = defineEmits(['refreshDataList'])

const getList = () => {
	//数据源下拉
	datasourceEntityListApi({}).then(res => {
		datasourceList.value = res.data
	})

	//模板组下拉
	templateGroupEntityListApi({}).then(res => {
		const data = res.data
		projectTemplateGroupList.value = data.filter((item: any) => item.type === 0)
		tableTemplateGroupList.value = data.filter((item: any) => item.type === 1)
		enumTemplateGroupList.value = data.filter((item: any) => item.type === 2)
	})

	//基类下拉
	baseClassEntityListApi({}).then(res => {
		baseClassList.value = res.data
	})
}

const state: FormOptions = reactive({
	submitApi: projectSubmitApi,
	detailApi: projectDetailApi,
	initBefore: getList,
	initFormData: {
		id: null,
		projectName: '',
		projectPackage: '',
		projectVersion: '',
		datasourceId: '',
		projectTemplateGroupId: '',
		tableTemplateGroupId: '',
		enumTemplateGroupId: '',
		backendPath: '',
		frontendPath: '',
		projectDesc: '',
		author: '',
		entityBaseClassId: '',
		voBaseClassId: '',
		generatorType: null
	},
	emit: emit,
	message: '操作成功，已经导入该项目引用数据源下的所有表，请到表管理中进行查看',
	duration: 2000
})
const dataRules = reactive({
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
const enumTemplateGroupList = ref([])
const baseClassList = ref([])

const { visible, dataForm, dataFormRef, init, submitHandle } = useSubmitForm(state)

defineExpose({
	init
})
</script>
