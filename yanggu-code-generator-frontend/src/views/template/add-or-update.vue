<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="id" prop="id">
				<el-input v-model="dataForm.id" placeholder="id"></el-input>
			</el-form-item>
			<el-form-item label="模板组id" prop="templateGroupId">
				<el-input v-model="dataForm.templateGroupId" placeholder="模板组id"></el-input>
			</el-form-item>
			<el-form-item label="模板名称" prop="templateName">
				<el-input v-model="dataForm.templateName" placeholder="模板名称"></el-input>
			</el-form-item>
			<el-form-item label="生成代码的路径" prop="generatorPath">
				<el-input v-model="dataForm.generatorPath" placeholder="生成代码的路径"></el-input>
			</el-form-item>
			<el-form-item label="模板描述" prop="templateDesc">
				<el-input v-model="dataForm.templateDesc" placeholder="模板描述"></el-input>
			</el-form-item>
			<el-form-item label="模板内容" prop="templateContent">
				<el-input v-model="dataForm.templateContent" placeholder="模板内容"></el-input>
			</el-form-item>
			<el-form-item label="模板类型（0-文件，1-文件夹）" prop="templateType">
				<el-input v-model="dataForm.templateType" placeholder="模板类型（0-文件，1-文件夹）"></el-input>
			</el-form-item>
			<el-form-item label="创建时间" prop="createTime">
				<el-input v-model="dataForm.createTime" placeholder="创建时间"></el-input>
			</el-form-item>
			<el-form-item label="修改时间" prop="updateTime">
				<el-input v-model="dataForm.updateTime" placeholder="修改时间"></el-input>
			</el-form-item>
			<el-form-item label="是否删除（0未删除, 1删除）" prop="isDelete">
				<el-input v-model="dataForm.isDelete" placeholder="是否删除（0未删除, 1删除）"></el-input>
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
import { templateDetailApi, templateSubmitApi } from '@/api/template'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	templateGroupId: '',
	templateName: '',
	generatorPath: '',
	templateDesc: '',
	templateContent: '',
	templateType: '',
	createTime: '',
	updateTime: '',
	isDelete: ''
})

const init = (id?: number) => {
	visible.value = true
	dataForm.id = null

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	if (id) {
		getTemplate(id)
	}
}

const getTemplate = (id: number) => {
	templateDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = ref({})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		templateSubmitApi(dataForm).then(() => {
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
