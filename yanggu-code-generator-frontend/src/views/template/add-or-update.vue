<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" width="60%" :close-on-click-modal="false" @closed="visible = false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
			<el-form-item label="模板名称" prop="templateName">
				<el-input v-model="dataForm.templateName" clearable placeholder="请输入模板名称"></el-input>
			</el-form-item>
			<el-form-item :label="dataForm.templateType === 0 ? '目录名称' : '文件名称'" prop="fileName">
				<el-input v-model="dataForm.fileName" clearable :placeholder="dataForm.templateType === 0 ? '请输入目录名称' : '请输入文件名称'"></el-input>
			</el-form-item>
			<el-form-item label="模板描述" prop="templateDesc">
				<el-input v-model="dataForm.templateDesc" clearable placeholder="请输入模板描述"></el-input>
			</el-form-item>
			<el-form-item label="模板类型" prop="templateType">
				<el-radio-group v-model="dataForm.templateType" disabled>
					<el-radio v-for="item in TEMPLATE_TYPES" :key="item.value" :value="item.value">
						{{ item.label }}
					</el-radio>
				</el-radio-group>
			</el-form-item>

			<!-- 模板内容编辑器 -->
			<!--			<el-form-item v-if="dataForm.templateType === 1" label="模板内容" prop="templateContent">-->
			<!--				<div class="code-editor">-->
			<!--					&lt;!&ndash; 根据是否全屏切换 el-scrollbar 的高度控制方式 &ndash;&gt;-->
			<!--					<el-scrollbar ref="codeMirrorRef" :max-height="!isFullscreen ? '300px' : 'none'" :class="{ 'full-screen-mode': isFullscreen }">-->
			<!--						&lt;!&ndash; 编辑器高度撑满 scrollbar 容器，触发内部滚动 &ndash;&gt;-->
			<!--						<code-mirror v-model="dataForm.templateContent"></code-mirror>-->
			<!--					</el-scrollbar>-->

			<!--					&lt;!&ndash; 全屏按钮 &ndash;&gt;-->
			<!--					<el-tooltip content="全屏编辑" effect="dark" placement="bottom">-->
			<!--						<el-icon :size="18" class="fullscreen-btn" @click="toggle()">-->
			<!--							<FullScreen />-->
			<!--						</el-icon>-->
			<!--					</el-tooltip>-->
			<!--				</div>-->
			<!--			</el-form-item>-->

			<!-- 文件上传 -->
			<el-form-item v-if="dataForm.templateType === 2" label="文件上传" prop="binaryOriginalFileName">
				<el-upload :limit="1" :file-list="fileList" :http-request="handleManualUpload">
					<el-button type="primary" :icon="Upload">点击上传</el-button>
				</el-upload>
			</el-form-item>
		</el-form>

		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { templateDetailApi, templateSubmitApi } from '@/api/template'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close, FullScreen, Upload } from '@element-plus/icons-vue'
import { useFullscreen } from '@vueuse/core'
import CodeMirror from '@/components/code-mirror/index.vue'

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	templateType: {
		type: Number,
		required: false
	}
})

const emit = defineEmits(['refreshDataList'])

const initAfterHandle = () => {
	if (dataForm.templateType == null) {
		dataForm.templateType = props.templateType
	}
	if (dataForm.templateType === 2) {
		fileList.value = [
			{
				name: dataForm.binaryOriginalFileName,
				url: dataForm.templateContent
			}
		]
	}
}

const state: FormOptions = reactive({
	visible: false,
	submitApi: templateSubmitApi,
	detailApi: templateDetailApi,
	initFormData: {
		id: null,
		templateGroupId: props.templateGroupId,
		templateName: '',
		fileName: '',
		templateDesc: '',
		templateContent: '',
		templateType: null,
		binaryOriginalFileName: ''
	},
	initAfter: initAfterHandle,
	emit
})

const dataRules = reactive({
	templateName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorPath: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	templateType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const codeMirrorRef = ref()
const { isFullscreen, toggle } = useFullscreen(codeMirrorRef)
const fileList = ref<any[]>([])

const { visible, dataForm, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

// watch(
// 	//添加模板类型监听（当类型变化时重新校验内容字段）
// 	() => dataForm.templateType,
// 	(newValue, oldValue) => {
// 		// console.log('newValue', newValue, 'oldValue', oldValue)
// 		if (newValue === 0) {
// 			dataRules.templateContent = []
// 			dataForm.templateContent = ''
// 			dataForm.binaryOriginalFileName = ''
// 		} else if (newValue === 1) {
// 			dataRules.templateContent = [{ required: dataForm.templateType === 1, message: '必填项不能为空', trigger: 'blur' }]
// 			dataForm.binaryOriginalFileName = ''
// 		} else if (newValue === 2) {
// 			dataRules.templateContent = []
// 			dataRules.binaryOriginalFileName = [{ required: dataForm.templateType === 2, message: '必填项不能为空', trigger: 'blur' }]
// 			dataForm.templateContent = ''
// 			dataForm.binaryOriginalFileName = ''
// 			if (oldValue != null) {
// 				fileList.value = []
// 			}
// 		}
// 	}
// )

const handleManualUpload = (options: any) => {
	const { file } = options
	// 直接读取为Base64数据URL
	const reader = new FileReader()
	reader.onload = e => {
		// 更新表单中的 templateContent 字段
		dataForm.templateContent = e.target?.result as string
		dataForm.binaryOriginalFileName = file.name
	}
	reader.readAsDataURL(file) // 读取为Base64数据URL
}

defineExpose({
	init
})
</script>

<style scoped>
.code-editor {
	width: 100%;
	position: relative;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
}

/* 全屏按钮 */
.fullscreen-btn {
	position: absolute;
	right: 10px;
	top: 0;
	z-index: 1000;
	padding: 5px;
	cursor: pointer;
}

/* 编辑器全屏样式 */
:deep(.full-screen-mode) {
	width: 100%;
	height: auto;
	background: white;
	padding: 0;
}
</style>
