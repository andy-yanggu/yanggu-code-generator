<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" width="60%" :close-on-click-modal="false" @closed="visible = false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px">
			<el-form-item v-if="state.dataForm.templateType === 1" label="模板名称" prop="templateName">
				<el-input v-model="state.dataForm.templateName" clearable placeholder="请输入模板名称"></el-input>
			</el-form-item>
			<el-form-item :label="state.dataForm.templateType === 0 ? '目录名称' : '文件名称'" prop="fileName">
				<el-input
					v-model="state.dataForm.fileName"
					clearable
					:placeholder="state.dataForm.templateType === 0 ? '请输入目录名称' : '请输入文件名称'"
				></el-input>
			</el-form-item>
			<el-form-item label="模板类型" prop="templateType">
				<el-radio-group v-model="state.dataForm.templateType" disabled>
					<el-radio v-for="item in TEMPLATE_TYPES" :key="item.value" :value="item.value">
						{{ item.label }}
					</el-radio>
				</el-radio-group>
			</el-form-item>
			<!-- 文件上传 -->
			<el-form-item v-if="state.dataForm.templateType === 2" label="文件上传" prop="binaryOriginalFileName">
				<el-upload :limit="1" :file-list="fileList" :http-request="handleManualUpload" :on-remove="handleRemove">
					<el-button type="primary" :icon="Upload">点击上传</el-button>
				</el-upload>
			</el-form-item>
			<el-form-item label="描述" prop="templateDesc">
				<el-input v-model="state.dataForm.templateDesc" clearable placeholder="请输入描述"></el-input>
			</el-form-item>
		</el-form>

		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { templateDetailApi, templateSubmitApi } from '@/api/template'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close, Upload } from '@element-plus/icons-vue'
import { UploadProps } from 'element-plus'

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	parentId: {
		type: Number,
		required: true
	},
	templateType: {
		type: Number,
		required: true
	}
})

const emit = defineEmits(['refreshDataList'])

const initAfterHandle = () => {
	state.dataForm.templateGroupId = props.templateGroupId
	state.dataForm.parentId = props.parentId
	state.dataForm.templateType = props.templateType
	if (state.dataForm.templateType === 2) {
		if (state.dataForm.id) {
			fileList.value = [
				{
					name: state.dataForm.binaryOriginalFileName,
					url: state.dataForm.templateContent
				}
			]
		} else {
			fileList.value = []
		}
	}
}

const submitBeforeHandle = () => {
	if (state.dataForm.templateType === 0) {
		state.dataForm.binaryOriginalFileName = ''
		state.dataForm.templateName = ''
		state.dataForm.templateContent = ''
	} else if (state.dataForm.templateType === 1) {
		state.dataForm.binaryOriginalFileName = ''
		// 删除模板内容字段
		delete state.dataForm.templateContent
	} else if (state.dataForm.templateType === 2) {
		state.dataForm.templateName = ''
	}
}

const state: FormOptions = reactive({
	visible: false,
	submitBefore: submitBeforeHandle,
	submitApi: templateSubmitApi,
	detailApi: templateDetailApi,
	dataForm: {
		id: null,
		templateGroupId: -1,
		parentId: -1,
		templateName: '',
		fileName: '',
		templateType: -1,
		templateDesc: '',
		binaryOriginalFileName: '',
		templateContent: ''
	},
	initAfter: initAfterHandle,
	emit
})

const validateTemplateName = (_: any, value: any, callback: any) => {
	if (state.dataForm.templateType != 0 && !value) {
		callback(new Error())
	} else {
		callback()
	}
}

const validateBinaryFile = (_: any, value: any, callback: any) => {
	if (state.dataForm.templateType === 2 && !value) {
		callback(new Error())
	} else {
		callback()
	}
}

const dataRules = reactive({
	templateName: [{ required: true, validator: validateTemplateName, message: '必填项不能为空', trigger: 'blur' }],
	fileName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	templateType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	binaryOriginalFileName: [{ required: true, validator: validateBinaryFile, message: '必填项不能为空', trigger: 'blur' }]
})

const fileList = ref<any[]>([])

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

const handleManualUpload = (options: any) => {
	const { file } = options
	// 直接读取为Base64数据URL
	const reader = new FileReader()
	reader.onload = e => {
		// 更新表单中的 templateContent 字段
		state.dataForm.templateContent = e.target?.result as string
		state.dataForm.binaryOriginalFileName = file.name
	}
	reader.readAsDataURL(file) // 读取为Base64数据URL
}

const handleRemove: UploadProps['onRemove'] = (file, uploadFiles) => {
	// 1. 清除文件数据
	state.dataForm.binaryOriginalFileName = ''
	state.dataForm.templateContent = ''

	// 2. 更新文件列表
	fileList.value = []
}

defineExpose({
	init
})
</script>

<style scoped></style>
