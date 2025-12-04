<template>
	<el-dialog v-model="visible" :title="'修改'" :close-on-click-modal="false" width="60%">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<form-divider title="基础信息"></form-divider>
			<el-row>
				<el-col :span="12">
					<el-form-item label="所属项目" prop="projectId">
						<el-select
							v-model="state.dataForm.projectId"
							:options="projectList"
							:props="{ label: 'projectName', value: 'id' }"
							clearable
							placeholder="请选择项目"
							disabled
						></el-select>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="所属模块" prop="moduleName">
						<el-select v-model="state.dataForm.moduleName" clearable filterable placeholder="请选择所属模块" style="width: 100%">
							<el-option
								v-for="item in projectList.find(temp => temp.id === state.dataForm.projectId)?.moduleList"
								:key="item.moduleName"
								:label="item.moduleName"
								:value="item.moduleName"
							>
								<option-label :label="item.moduleName" :desc="item.moduleDesc"></option-label>
							</el-option>
						</el-select>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="数据库名" prop="databaseName">
						<el-input v-model="state.dataForm.databaseName" clearable placeholder="请输入数据库名"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="表名" prop="tableName">
						<el-input v-model="state.dataForm.tableName" clearable placeholder="请输入表名" disabled></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="类名" prop="className">
						<el-input v-model="state.dataForm.className" clearable placeholder="请输入类名"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="注释" prop="tableComment">
						<el-input v-model="state.dataForm.tableComment" clearable placeholder="请输入说明"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="作者" prop="author">
						<el-input v-model="state.dataForm.author" clearable placeholder="请输入作者"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="版本" prop="version">
						<el-input v-model="state.dataForm.version" clearable placeholder="请输入版本"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<form-divider title="功能配置"></form-divider>
			<el-row>
				<el-col :span="12">
					<el-form-item label="功能名" prop="functionName">
						<el-input v-model="state.dataForm.functionName" clearable placeholder="请输入功能名"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="权限标识" prop="permissionFlag">
						<el-input v-model="state.dataForm.permissionFlag" clearable placeholder="请输入权限标识"></el-input> </el-form-item
				></el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="表单布局" prop="formLayout">
						<el-radio-group v-model="state.dataForm.formLayout" :options="FORM_LAYOUT_TYPES"></el-radio-group>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="弹窗方式" prop="popupType">
						<el-radio-group v-model="state.dataForm.popupType" :options="TABLE_POPUP_TYPE_TYPES"></el-radio-group>
					</el-form-item>
				</el-col>
			</el-row>
			<el-form-item label="生成功能" prop="generatorFunction">
				<el-checkbox-group v-model="state.dataForm.generatorFunction" :options="TABLE_GENERATOR_FUNCTION_TYPES"></el-checkbox-group>
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
import { FORM_LAYOUT_TYPES, TABLE_GENERATOR_FUNCTION_TYPES, TABLE_POPUP_TYPE_TYPES } from '@/constant/enum'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import { genProjectApi, genTableApi } from '@/api'
import { FormOptions, GenProjectEntity, GenTableEntity } from '@/types'
import FormDivider from '@/components/form/divider/index.vue'
import OptionLabel from '@/components/option/label/index.vue'

defineOptions({
	name: 'GenTableForm'
})

const emit = defineEmits(['refreshDataList'])

const projectList = ref([] as GenProjectEntity[])

const state = reactive({
	submitApi: genTableApi.submit,
	detailApi: genTableApi.detail,
	initBefore: () => {
		genProjectApi.entityList().then(data => {
			projectList.value = data
		})
	},
	dataForm: {
		id: -1,
		tableName: '',
		databaseName: '',
		className: '',
		tableComment: '',
		projectId: -1,
		author: '',
		version: '',
		moduleName: '',
		functionName: '',
		permissionFlag: '',
		formLayout: '',
		popupType: '',
		generatorFunction: [0, 1, 2, 3, 4, 5]
	},
	emit
} as FormOptions<GenTableEntity>)

const dataRules = reactive({
	projectId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	tableName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	databaseName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	className: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	tableComment: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	functionName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	formLayout: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	popupType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorFunction: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
