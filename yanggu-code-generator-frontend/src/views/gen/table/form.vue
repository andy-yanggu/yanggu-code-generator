<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false" width="60%">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="130px" @keyup.enter="submitHandle()">
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

			<form-divider title="基类配置"></form-divider>
			<el-form-item prop="entityBaseClassId" label="Entity基类">
				<el-select v-model="state.dataForm.entityBaseClassId" placeholder="请选择Entity基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id" :label="`${item.packageName}.${item.className}`" :value="item.id">
						<option-label :label="`${item.packageName}.${item.className}`" :desc="item.remark"></option-label>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item prop="voBaseClassId" label="VO基类">
				<el-select v-model="state.dataForm.voBaseClassId" placeholder="请选择VO基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id" :label="`${item.packageName}.${item.className}`" :value="item.id">
						<option-label :label="`${item.packageName}.${item.className}`" :desc="item.remark"></option-label>
					</el-option>
				</el-select>
			</el-form-item>

			<!-- 表模板组属性表单 -->
			<template v-if="isNotEmpty(tableTemplateGroupPropertyList)">
				<form-divider title="表模板组属性"></form-divider>
				<template-group-property-form
					v-model:form-data="state.dataForm.templateGroupPropertyData"
					:property-list="tableTemplateGroupPropertyList"
				></template-group-property-form>
			</template>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { FORM_LAYOUT_TYPES, TABLE_GENERATOR_FUNCTION_TYPES, TABLE_POPUP_TYPE_TYPES } from '@/constant/enum'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import { genBaseClassApi, genProjectApi, genTableApi, genTemplateGroupPropertyApi } from '@/api'
import { FormOptions, GenBaseClassEntity, GenProjectEntity, GenTableEntity, GenTemplateGroupPropertyEntity } from '@/types'
import FormDivider from '@/components/form/divider/index.vue'
import OptionLabel from '@/components/option/label/index.vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/property-form.vue'
import { isNotEmpty } from '@/utils/tool'
import { FormItemRule } from 'element-plus'

defineOptions({
	name: 'GenTableForm'
})

const emit = defineEmits(['refreshDataList'])

// 初始化表单数据
const initFormData = (): GenTableEntity => ({
	id: null,
	tableName: '',
	databaseName: '',
	className: '',
	tableComment: '',
	projectId: -1,
	author: '',
	version: '',
	moduleName: '',
	functionName: '',
	voBaseClassId: '',
	entityBaseClassId: '',
	permissionFlag: '',
	formLayout: '',
	popupType: '',
	tableTemplateGroupId: '',
	templateGroupPropertyData: {},
	generatorFunction: [0, 1, 2, 3, 4, 5]
})

const projectList = ref([] as GenProjectEntity[])
const baseClassList = ref([] as GenBaseClassEntity[])
const tableTemplateGroupPropertyList = ref([] as GenTemplateGroupPropertyEntity[])

const state = reactive({
	// 标题主体
	titleSubject: '表',
	submitApi: genTableApi.submit,
	detailApi: genTableApi.detail,
	initBefore: () => {
		// 项目下拉
		genProjectApi.entityList().then(data => {
			projectList.value = data
		})
		// 基类下拉
		genBaseClassApi.entityList().then(data => {
			baseClassList.value = data
		})
	},
	initFormData,
	dataForm: initFormData(),
	initAfter: () => {
		genTemplateGroupPropertyApi.entityList({ templateGroupId: state.dataForm.tableTemplateGroupId }).then(data => {
			tableTemplateGroupPropertyList.value = data
		})
	},
	emit
} as FormOptions<GenTableEntity>)

const dataRules = computed(() => {
	const rules: Record<keyof GenTableEntity | string, FormItemRule[]> = {
		projectId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		tableName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		databaseName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		className: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		tableComment: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		functionName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		formLayout: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		popupType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
		generatorFunction: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
	}

	// 模板组属性校验
	tableTemplateGroupPropertyList.value.forEach(item => {
		if (item.required === 1) {
			rules[`templateGroupPropertyData.${item.propKey}`] = [{ required: true, message: `${item.propTitle}不能为空`, trigger: 'blur' }]
		}
	})
	return rules
})

const { visible, dataFormRef, init, submitHandle, submitLoading, dialogTitle } = useSubmitForm(state)

defineExpose({
	init
})
</script>
