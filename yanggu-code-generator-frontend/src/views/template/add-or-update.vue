<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" width="60%" :close-on-click-modal="false" @closed="visible = false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
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
			<el-form-item v-show="dataForm.templateType === 0" label="模板内容" prop="templateContent">
				<div class="code-editor" :style="!dataForm.templateContent && !isFullscreen ? { height: '250px' } : {}">
					<codemirror
						ref="codeMirrorRef"
						v-model="dataForm.templateContent"
						:options="cmOptions"
						class="code-mirror"
						:class="{ 'full-screen-mode': isFullscreen }"
					></codemirror>
				</div>
				<el-tooltip content="全屏编辑" effect="dark" placement="bottom">
					<el-icon :size="18" class="fullscreen-btn" @click="toggle()">
						<FullScreen></FullScreen>
					</el-icon>
				</el-tooltip>
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
import { Codemirror } from 'vue-codemirror'
import { EditorView, keymap } from '@codemirror/view'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close, FullScreen } from '@element-plus/icons-vue'
import { useFullscreen } from '@vueuse/core'

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	}
})

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
	visible: false,
	submitApi: templateSubmitApi,
	detailApi: templateDetailApi,
	initFormData: {
		id: null,
		templateGroupId: props.templateGroupId,
		templateName: '',
		generatorPath: '',
		templateDesc: '',
		templateContent: '',
		templateType: null
	},
	emit
})

const dataRules = reactive({
	templateName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorPath: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	templateType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const codeMirrorRef = ref(null)
const { isFullscreen, toggle } = useFullscreen(codeMirrorRef)

// 修改cmOptions配置
const cmOptions = reactive({
	extensions: [keymap.of([]), EditorView.contentAttributes.of({ autocomplete: 'on' })],
	lineNumbers: true,
	tabSize: 2,
	autocompletion: true
})

const { visible, dataForm, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

watch(
	//添加模板类型监听（当类型变化时重新校验内容字段）
	() => dataForm.templateType,
	(newValue, _) => {
		if (newValue === 0) {
			dataRules.templateContent = [{ required: dataForm.templateType === 0, message: '必填项不能为空', trigger: 'blur' }]
		} else if (newValue === 1) {
			dataRules.templateContent = []
			dataForm.templateContent = ''
		}
	}
)

defineExpose({
	init
})
</script>

<style scoped>
.code-editor {
	width: 100%; /* 继承父容器宽度 */
	position: relative;
	height: auto; /* 当有内容时保持自适应 */
	min-height: 40px; /* 保持与常规输入框一致的最小高度 */
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	overflow: hidden;
	overflow-y: auto; /* 新增纵向滚动条 */
}

.code-mirror {
	height: 100%;
	min-height: 100%; /* 确保编辑器高度足够触发滚动 */
}

.fullscreen-btn {
	position: absolute;
	right: 10px;
	top: 10px;
	z-index: 1000;
	padding: 5px;
	cursor: pointer;
}

:deep(.full-screen-mode) {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	z-index: 9999;
	background: white;
	margin: 0 !important;
	padding: 20px;
}
</style>
