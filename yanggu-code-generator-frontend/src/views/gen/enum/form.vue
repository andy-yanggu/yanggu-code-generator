<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="state.dataForm.projectId" clearable :disabled="state.dataForm.id != null" placeholder="请选择项目">
					<el-option v-for="item in projectList" :key="item.id" :value="item.id" :label="item.projectName">{{ item.projectName }}</el-option>
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
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { genEnumApi, GenEnumEntity } from '@/api/gen/enum'
import { genProjectApi, GenProjectEntity } from '@/api/gen/project'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

const projectList = ref([] as GenProjectEntity[])
const getProjectList = () => {
	genProjectApi.entityList().then(data => {
		projectList.value = data
	})
}

const emit = defineEmits(['refreshDataList'])
const state = reactive({
	submitApi: genEnumApi.submit,
	detailApi: genEnumApi.detail,
	initBefore: getProjectList,
	dataForm: {
		id: -1,
		projectId: '',
		enumName: '',
		enumDesc: ''
	},
	emit
} as FormOptions<GenEnumEntity>)

const dataRules = reactive({
	enumName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	enumDesc: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
