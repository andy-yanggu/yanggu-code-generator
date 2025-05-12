<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
	                <el-form-item label="id" prop="id">
                <el-input v-model="dataForm.id" placeholder="请输入id"></el-input>
            </el-form-item>
            <el-form-item label="表名" prop="tableName">
                <el-input v-model="dataForm.tableName" placeholder="请输入表名"></el-input>
            </el-form-item>
            <el-form-item label="数据库名" prop="databaseName">
                <el-input v-model="dataForm.databaseName" placeholder="请输入数据库名"></el-input>
            </el-form-item>
            <el-form-item label="类名" prop="className">
                <el-input v-model="dataForm.className" placeholder="请输入类名"></el-input>
            </el-form-item>
            <el-form-item label="说明" prop="tableComment">
                <el-input v-model="dataForm.tableComment" placeholder="请输入说明"></el-input>
            </el-form-item>
            <el-form-item label="项目ID" prop="projectId">
                <el-input v-model="dataForm.projectId" placeholder="请输入项目ID"></el-input>
            </el-form-item>
            <el-form-item label="作者" prop="author">
                <el-input v-model="dataForm.author" placeholder="请输入作者"></el-input>
            </el-form-item>
            <el-form-item label="项目版本号" prop="version">
                <el-input v-model="dataForm.version" placeholder="请输入项目版本号"></el-input>
            </el-form-item>
            <el-form-item label="功能名" prop="functionName">
                <el-input v-model="dataForm.functionName" placeholder="请输入功能名"></el-input>
            </el-form-item>
            <el-form-item label="表单布局  1：一列   2：两列" prop="formLayout">
                <el-input v-model="dataForm.formLayout" placeholder="请输入表单布局  1：一列   2：两列"></el-input>
            </el-form-item>
            <el-form-item label="创建时间" prop="createTime">
                <el-input v-model="dataForm.createTime" placeholder="请输入创建时间"></el-input>
            </el-form-item>
            <el-form-item label="修改时间" prop="updateTime">
                <el-input v-model="dataForm.updateTime" placeholder="请输入修改时间"></el-input>
            </el-form-item>
            <el-form-item label="是否删除（0未删除, 1删除）" prop="isDelete">
                <el-input v-model="dataForm.isDelete" placeholder="请输入是否删除（0未删除, 1删除）"></el-input>
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
import { tableDetailApi, tableSubmitApi } from '@/api/table'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	tableName: '',
	databaseName: '',
	className: '',
	tableComment: '',
	projectId: '',
	author: '',
	version: '',
	functionName: '',
	formLayout: '',
	createTime: '',
	updateTime: '',
	isDelete: ''})

const init = (id?: number) => {
	visible.value = true
	dataForm.id = null

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	if (id) {
		getTable(id)
	}
}

const getTable = (id: number) => {
	tableDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = ref({
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		tableSubmitApi(dataForm).then(() => {
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
