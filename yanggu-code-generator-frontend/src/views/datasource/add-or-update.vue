<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="连接名称" prop="connName">
				<el-input v-model="state.dataForm.connName" clearable placeholder="请输入连接名称"></el-input>
			</el-form-item>
			<el-form-item label="数据库类型" prop="dbType">
				<el-select v-model="state.dataForm.dbType" clearable placeholder="请选择数据库类型">
					<el-option v-for="item in DB_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
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
import { datasourceDetailApi, datasourceSubmitApi } from '@/api/datasource'
import { DB_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
	submitApi: datasourceSubmitApi,
	detailApi: datasourceDetailApi,
	dataForm: {
		id: '',
		dbType: '',
		connName: '',
		connUrl: '',
		username: '',
		password: '',
		datasourceDesc: ''
	},
	emit
})

const dataRules = reactive({
	dbType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	connName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	connUrl: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	username: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	password: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
