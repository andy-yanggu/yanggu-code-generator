<template>
	<el-dialog v-model="visible" :title="dialogTitle()" width="60%" :close-on-click-modal="false" @closed="visible = false">
		<el-form
			ref="dataFormRef"
			:model="state.dataForm"
			:rules="dataRules"
			label-width="110px"
			:validate-on-rule-change="false"
			@keyup.enter="submitHandle()"
		>
			<el-form-item label="路径" prop="templatePath">
				<el-input v-model="state.dataForm.templatePath" disabled></el-input>
			</el-form-item>
			<el-form-item :label="fileName" prop="fileName">
				<el-input v-model="state.dataForm.fileName" clearable :placeholder="`请输入${fileName}`"></el-input>
			</el-form-item>
			<el-form-item label="模板类型" prop="templateType">
				<el-radio-group v-model="state.dataForm.templateType" :options="TEMPLATE_TYPES" disabled></el-radio-group>
			</el-form-item>
			<template v-if="state.dataForm.templateType === 0">
				<el-form-item prop="conditionExpression">
					<template #label>
						<form-label-tooltip tooltip="使用Aviator表达式，需要返回布尔值" label="条件表达式"></form-label-tooltip>
					</template>
					<el-input v-model="state.dataForm.conditionExpression" clearable placeholder="请输入条件表达式"></el-input>
				</el-form-item>
			</template>
			<template v-if="state.dataForm.templateType === 1">
				<el-form-item label="模板名称" prop="templateName">
					<el-input v-model="state.dataForm.templateName" clearable placeholder="请输入模板名称"></el-input>
				</el-form-item>
				<el-form-item prop="conditionExpression">
					<template #label>
						<form-label-tooltip tooltip="使用Aviator表达式，需要返回布尔值" label="条件表达式"></form-label-tooltip>
					</template>
					<el-input v-model="state.dataForm.conditionExpression" clearable placeholder="请输入条件表达式"></el-input>
				</el-form-item>
			</template>
			<template v-else-if="state.dataForm.templateType === 2">
				<!-- 文件上传 -->
				<el-form-item v-if="state.dataForm.templateType === 2" label="文件上传" prop="binaryOriginalFileName">
					<el-upload :limit="1" :file-list="fileList" :on-exceed="handleExceed" :http-request="handleManualUpload" :on-remove="handleRemove">
						<el-button type="primary" :icon="Upload">点击上传</el-button>
					</el-upload>
				</el-form-item>
			</template>
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
import { computed, reactive, ref } from 'vue'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { useSubmitForm } from '@/hooks'
import { Check, Close, Upload } from '@element-plus/icons-vue'
import { FormItemRule, UploadProps } from 'element-plus'
import { genTemplateApi } from '@/api'
import { FormOptions, GenTemplateEntity, Key } from '@/types'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'GenTemplateForm'
})

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
	},
	templatePath: {
		type: String,
		required: true
	}
})

const emit = defineEmits(['refreshDataList'])

const initAfterHandle = () => {
	state.dataForm.templateGroupId = props.templateGroupId
	state.dataForm.parentId = props.parentId
	state.dataForm.templateType = props.templateType
	state.dataForm.templatePath = props.templatePath
	if (state.dataForm.templateType === 2) {
		if (state.dataForm.id) {
			fileList.value = [
				{
					name: state.dataForm.binaryOriginalFileName
				} as File
			]
		} else {
			fileList.value = []
		}
	}
}

// 初始化表单数据
const initFormData = (): GenTemplateEntity => ({
	id: '',
	templateGroupId: -1,
	parentId: -1,
	templateName: '',
	fileName: '',
	templateType: -1,
	templateDesc: '',
	binaryOriginalFileName: '',
	conditionExpression: '',
	templateContent: '',
	templatePath: ''
})

const state = reactive({
	// 标题主体
	titleSubject: '模板',
	submitApi: genTemplateApi.submit,
	detailApi: (id: Key) => genTemplateApi.detailData({ id }),
	initFormData,
	dataForm: initFormData(),
	initAfter: initAfterHandle,
	emit
} as FormOptions<GenTemplateEntity>)

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

const fileName = computed(() => {
	if (state.dataForm.templateType === 0) {
		return '目录名称'
	} else {
		return '文件名称'
	}
})

const dataRules = computed(() => {
	const constRules: Record<string, FormItemRule[]> = {
		templateName: [{ required: true, validator: validateTemplateName, message: '模板名称不能为空', trigger: 'blur' }],
		fileName: [{ required: true, message: `${fileName.value}不能为空`, trigger: 'blur' }],
		templateType: [{ required: true, message: '模板类型不能为空', trigger: 'blur' }],
		binaryOriginalFileName: [{ required: true, validator: validateBinaryFile, message: '文件不能为空', trigger: 'blur' }]
	}
	return constRules
})

const fileList = ref([] as File[])

const { visible, dataFormRef, init, submitHandle, submitLoading, dialogTitle } = useSubmitForm(state)

// 统一的文件处理方法
const processFile = (file: File) => {
	return new Promise<void>(resolve => {
		const reader = new FileReader()
		reader.onload = e => {
			// 更新表单中的 templateContent 字段
			state.dataForm.templateContent = e.target?.result as string
			state.dataForm.binaryOriginalFileName = file.name

			// 更新文件列表用于显示（添加状态以便显示成功图标）
			fileList.value = [file]
			resolve()
		}
		reader.readAsDataURL(file)
	})
}

// 处理文件上传
const handleManualUpload = async (options: any) => {
	const { file, onSuccess } = options

	try {
		await processFile(file)

		// 通知上传成功，这会触发绿色小勾的显示
		if (onSuccess) {
			onSuccess({}, file)
		}
	} catch (error) {
		console.error('文件处理失败:', error)
	}
}

// 处理超出限制的情况（自动替换文件）
const handleExceed: UploadProps['onExceed'] = files => {
	// 清除之前文件的数据
	state.dataForm.binaryOriginalFileName = ''
	state.dataForm.templateContent = ''
	fileList.value = []

	// 处理新文件
	const file = files[0] as File
	processFile(file)
}

// 处理文件移除
const handleRemove: UploadProps['onRemove'] = () => {
	// 清除文件数据
	state.dataForm.binaryOriginalFileName = ''
	state.dataForm.templateContent = ''

	// 清空文件列表
	fileList.value = []
}

defineExpose({
	init
})
</script>

<style scoped></style>
