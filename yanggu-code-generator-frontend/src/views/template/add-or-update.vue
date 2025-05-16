<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="模板名称" prop="templateName">
				<el-input v-model="dataForm.templateName" placeholder="请输入模板名称"></el-input>
			</el-form-item>
			<el-form-item label="路径" prop="generatorPath">
				<el-input v-model="dataForm.generatorPath" placeholder="请输入生成代码的路径"></el-input>
			</el-form-item>
			<el-form-item label="模板类型" prop="templateType">
				<el-radio-group v-model="dataForm.templateType" :disabled="dataForm.id">
					<el-radio v-for="item in TEMPLATE_TYPES" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
				</el-radio-group>
			</el-form-item>
			<el-form-item label="模板描述" prop="templateDesc">
				<el-input v-model="dataForm.templateDesc" placeholder="请输入模板描述"></el-input>
			</el-form-item>
			<el-form-item label="模板内容" prop="templateContent">
				<el-input v-model="dataForm.templateContent" type="textarea" autosize placeholder="请输入模板内容"></el-input>
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
import { TEMPLATE_TYPES } from '@/constant/enum'

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	}
})

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: null,
	templateGroupId: props.templateGroupId,
	templateName: '',
	generatorPath: '',
	templateDesc: '',
	templateContent: '',
	templateType: ''
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

const dataRules = ref({
	templateName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorPath: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	templateContent: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	templateType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

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
