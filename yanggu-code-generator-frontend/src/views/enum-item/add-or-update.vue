<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="枚举项名称" prop="enumItemName">
				<el-input v-model="dataForm.enumItemName" placeholder="请输入枚举项名称"></el-input>
			</el-form-item>
			<el-form-item label="枚举项编码" prop="enumItemCode">
				<el-input v-model="dataForm.enumItemCode" placeholder="请输入枚举项编码"></el-input>
			</el-form-item>
			<el-form-item label="枚举项描述" prop="enumItemDesc">
				<el-input v-model="dataForm.enumItemDesc" placeholder="请输入枚举项描述"></el-input>
			</el-form-item>
			<el-form-item label="枚举项排序" prop="enumItemOrder">
				<el-input-number v-model="dataForm.enumItemOrder" :min="0" size="small"></el-input-number>
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
import { enumItemDetailApi, enumItemSubmitApi } from '@/api/enumItem'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: null,
	enumId: -1,
	enumItemName: '',
	enumItemCode: '',
	enumItemDesc: '',
	enumItemOrder: 0
})

const init = (enumId: number, id?: number) => {
	dataForm.enumId = enumId
	visible.value = true
	dataForm.id = null

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	if (id) {
		getEnumItem(id)
	}
}

const getEnumItem = (id: number) => {
	enumItemDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = reactive({
	enumItemName: [{ required: true, message: '请输入枚举项名称', trigger: 'blur' }],
	enumItemCode: [{ required: true, message: '请输入枚举项编码', trigger: 'blur' }],
	enumItemDesc: [{ required: true, message: '请输入枚举项描述', trigger: 'blur' }]
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		enumItemSubmitApi(dataForm).then(() => {
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
