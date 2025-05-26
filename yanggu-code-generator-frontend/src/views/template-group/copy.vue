<template>
	<el-dialog v-model="visible" title="复制" :close-on-click-modal="false" width="35%">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="名称" prop="groupName">
				<el-input v-model="dataForm.groupName" clearable placeholder="请输入名称"></el-input>
			</el-form-item>
			<el-form-item label="类型" prop="type">
				<el-select v-model="dataForm.type" disabled clearable placeholder="请选择模板组类型" style="width: 100%">
					<el-option v-for="item in TEMPLATE_GROUP_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="描述" prop="groupDesc">
				<el-input v-model="dataForm.groupDesc" clearable placeholder="请输入描述"></el-input>
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
import { templateGroupDetailApi, copyTemplateApi } from '@/api/templateGroup'

import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	groupName: '',
	type: null,
	groupDesc: '',
	createTime: ''
})

const init = (id: number) => {
	visible.value = true
	dataForm.id = ''
	dataForm.createTime = ''
	getTemplateGroup(id)
}

const getTemplateGroup = (id: number) => {
	templateGroupDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
		dataForm.groupName = dataForm.groupName + '_复制'
	})
}

const dataRules = reactive({
	groupName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
	type: [{ required: true, message: '类型不能为空', trigger: 'blur' }]
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		copyTemplateApi(dataForm).then(() => {
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
