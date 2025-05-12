<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="基类编码" prop="code">
				<el-input v-model="dataForm.code" placeholder="请输入基类编码"></el-input>
			</el-form-item>
			<el-form-item label="基类包名" prop="packageName">
				<el-input v-model="dataForm.packageName" placeholder="请输入基类包名"></el-input>
			</el-form-item>
			<el-form-item label="基类字段" prop="fields">
				<el-input v-model="dataForm.fields" placeholder="请输入基类字段，多个用英文逗号分隔"></el-input>
			</el-form-item>
			<el-form-item label="备注" prop="remark">
				<el-input v-model="dataForm.remark" placeholder="请输入备注"></el-input>
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
import { baseClassDetailApi, baseClassSubmitApi } from '@/api/baseClass'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	packageName: '',
	code: '',
	fields: '',
	remark: '',
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
		getBaseClass(id)
	}
}

const getBaseClass = (id: number) => {
	baseClassDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = ref({
	packageName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	code: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	fields: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		baseClassSubmitApi(dataForm).then(() => {
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
