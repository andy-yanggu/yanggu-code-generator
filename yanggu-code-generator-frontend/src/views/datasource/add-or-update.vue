<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="连接名称" prop="connName">
				<el-input v-model="dataForm.connName" placeholder="请输入连接名称"></el-input>
			</el-form-item>
			<el-form-item label="数据库类型" prop="dbType">
				<el-select v-model="dataForm.dbType" clearable placeholder="请选择数据库类型">
					<el-option v-for="item in DB_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="URL" prop="connUrl">
				<el-input v-model="dataForm.connUrl" placeholder="请输入URL"></el-input>
			</el-form-item>
			<el-form-item label="用户名" prop="username">
				<el-input v-model="dataForm.username" placeholder="请输入用户名"></el-input>
			</el-form-item>
			<el-form-item label="密码" prop="password">
				<el-input v-model="dataForm.password" type="password" show-password placeholder="请输入密码"></el-input>
			</el-form-item>
			<el-form-item label="描述" prop="datasourceDesc">
				<el-input v-model="dataForm.datasourceDesc" placeholder="请输入描述"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { datasourceDetailApi, datasourceSubmitApi } from '@/api/datasource'
import { DB_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
	submitApi: datasourceSubmitApi,
	detailApi: datasourceDetailApi,
	initFormData: {
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

const { visible, dataForm, dataFormRef, init, submitHandle } = useSubmitForm(state)

defineExpose({
	init
})
</script>
