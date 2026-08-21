<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false">
		<el-form
			ref="dataFormRef"
			:model="state.dataForm"
			:rules="dataRules"
			label-width="140px"
			:validate-on-rule-change="false"
			@keyup.enter="submitHandle()">
			<form-divider v-if="isNotEmpty(enumTemplateGroupPropertyList)" title="基础属性"></form-divider>
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="state.dataForm.projectId" clearable :disabled="isNotBlank(state.dataForm.id)" placeholder="请选择项目">
					<el-option v-for="projectItem in projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
						<option-label :label="projectItem.projectName" :desc="projectItem.projectDesc"></option-label>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item prop="enumName">
				<template #label>
					<form-label-tooltip label="枚举名称" tooltip="使用英文小写字母，单词之间使用'-'拼接；在同一个项目下唯一"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.enumName" clearable placeholder="请输入枚举名称"></el-input>
			</el-form-item>
			<el-form-item label="枚举描述" prop="enumDesc">
				<el-input v-model="state.dataForm.enumDesc" clearable placeholder="请输入枚举描述"></el-input>
			</el-form-item>
			<!-- 枚举模板组属性表单 -->
			<template v-if="isNotEmpty(enumTemplateGroupPropertyList)">
				<form-divider title="枚举模板组属性"></form-divider>
				<template-group-property-form
					:key="state.dataForm.projectId"
					v-model:form-data="state.dataForm.templateGroupPropertyData"
					:property-list="enumTemplateGroupPropertyList"></template-group-property-form>
			</template>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { genEnumApi, genProjectApi, genTemplateGroupPropertyApi } from '@/api'
import { FormOptions, GenEnumEntity, GenProjectEntity, GenTemplateGroupPropertyEntity } from '@/types'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import { isBlank, isNotBlank, isNotEmpty } from '@/utils/tool'
import FormDivider from '@/components/form/divider/index.vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/property-form.vue'
import OptionLabel from '@/components/option/label/index.vue'
import { FormItemRule } from 'element-plus'

defineOptions({
	name: 'GenEnumForm'
})

const projectList = ref([] as GenProjectEntity[])
const enumTemplateGroupPropertyList = ref([] as GenTemplateGroupPropertyEntity[])

// 初始化表单数据
const initFormData = (): GenEnumEntity => ({
	id: '',
	projectId: '',
	enumName: '',
	enumDesc: '',
	templateGroupPropertyData: {}
})

const emit = defineEmits(['refreshDataList'])
const state = reactive({
	// 标题主体
	titleSubject: '枚举',
	submitApi: genEnumApi.submit,
	detailApi: genEnumApi.detail,
	initBefore: () => {
		genProjectApi.entityList().then(data => {
			projectList.value = data
		})
	},
	initFormData,
	dataForm: initFormData(),
	emit
} as FormOptions<GenEnumEntity>)

watch(
	() => state.dataForm.projectId,
	projectId => {
		enumTemplateGroupPropertyList.value = []
		if (!projectId) {
			return
		}

		const project = projectList.value.find(item => item.id === projectId)
		if (!project || isBlank(project.enumTemplateGroupId)) {
			return
		}

		genTemplateGroupPropertyApi
			.entityList({
				templateGroupId: project.enumTemplateGroupId
			})
			.then(data => {
				enumTemplateGroupPropertyList.value = data
			})
	},
	{ immediate: true }
)

const dataRules = reactive({
	enumName: [{ required: true, message: '枚举名称不能为空', trigger: 'blur' }],
	enumDesc: [{ required: true, message: '枚举描述不能为空', trigger: 'blur' }],
	projectId: [{ required: true, message: '项目不能为空', trigger: 'blur' }]
} as Record<keyof GenEnumEntity, FormItemRule[]>)

const { visible, dataFormRef, init, submitHandle, submitLoading, dialogTitle } = useSubmitForm(state)

defineExpose({
	init
})
</script>
