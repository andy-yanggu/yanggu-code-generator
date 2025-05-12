<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="主键ID" prop="id">
				<el-input v-model="dataForm.id" placeholder="主键ID"></el-input>
			</el-form-item>
			<el-form-item label="模板组名称" prop="groupName">
				<el-input v-model="dataForm.groupName" placeholder="模板组名称"></el-input>
			</el-form-item>
			<el-form-item label="模板组类型（0-项目，1-表）" prop="type">
				<el-input v-model="dataForm.type" placeholder="模板组类型（0-项目，1-表）"></el-input>
			</el-form-item>
			<el-form-item label="模板组描述" prop="groupDesc">
				<el-input v-model="dataForm.groupDesc" placeholder="模板组描述"></el-input>
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
import { templateGroupDetailApi, templateGroupSubmitApi } from '@/api/templateGroup'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	groupName: '',
	type: '',
	groupDesc: '',
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
		getTemplateGroup(id)
	}
}

const getTemplateGroup = (id: number) => {
	templateGroupDetailApi(id).then(res => {
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

		templateGroupSubmitApi(dataForm).then(() => {
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
