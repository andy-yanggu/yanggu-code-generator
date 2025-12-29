<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item prop="connName">
				<template #label>
					<form-label-tooltip label="连接名称" tooltip="连接名称具有唯一性"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.connName" clearable placeholder="请输入连接名称"></el-input>
			</el-form-item>
			<el-form-item label="数据库类型" prop="dbType">
				<el-select v-model="state.dataForm.dbType" :options="DB_TYPES" filterable clearable placeholder="请选择数据库类型"></el-select>
			</el-form-item>
			<el-form-item label="URL" prop="connUrl">
				<el-input v-model="state.dataForm.connUrl" clearable placeholder="请输入URL"></el-input>
			</el-form-item>
			<el-form-item label="用户名" prop="username">
				<el-input v-model="state.dataForm.username" clearable placeholder="请输入用户名"></el-input>
			</el-form-item>
			<el-form-item label="密码" prop="password">
				<el-input v-model="state.dataForm.password" type="password" show-password clearable placeholder="请输入密码"></el-input>
			</el-form-item>
			<el-form-item label="描述" prop="datasourceDesc">
				<el-input v-model="state.dataForm.datasourceDesc" clearable placeholder="请输入描述"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { genDatasourceApi } from '@/api'
import { FormOptions, GenDatasourceEntity } from '@/types'
import { DB_TYPES } from '@/constant/enum'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'GenDatasourceForm'
})

// 初始化表单数据
const initFormData = (): GenDatasourceEntity => ({
	id: null,
	dbType: '',
	connName: '',
	connUrl: '',
	username: '',
	password: '',
	datasourceDesc: ''
})

const emit = defineEmits(['refreshDataList'])

const state = reactive({
	submitApi: genDatasourceApi.submit,
	detailApi: genDatasourceApi.detail,
	initFormData,
	dataForm: initFormData(),
	initAfter: () => {
		if (formType.value === 'copy') {
			state.dataForm.connName = state.dataForm.connName + '_复制'
			state.dataForm.id = null
		}
	},
	emit
} as FormOptions<GenDatasourceEntity>)

const dataRules = reactive({
	dbType: [{ required: true, message: '数据库类型不能为空', trigger: 'blur' }],
	connName: [{ required: true, message: '连接名称不能为空', trigger: 'blur' }],
	connUrl: [{ required: true, message: 'URL不能为空', trigger: 'blur' }],
	username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
	password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, formType, dialogTitle, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
