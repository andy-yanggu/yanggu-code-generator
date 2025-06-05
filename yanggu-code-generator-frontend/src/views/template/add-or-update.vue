<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" width="60%" :close-on-click-modal="false" @closed="handleDialogClosed">
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
			<el-form-item v-if="dataForm.templateType === 0" label="模板内容" prop="templateContent">
				<div
					class="code-editor"
					:class="{ 'full-screen': isFullScreen }"
					:style="!dataForm.templateContent && !isFullScreen ? { height: '250px' } : {}"
				>
					<codemirror v-model="dataForm.templateContent" :options="cmOptions" class="code-mirror" @ready="handleEditorReady" />
					<el-button v-if="isFullScreen" class="confirm-btn" type="primary" @click="toggleFullscreen">退出全屏</el-button>
				</div>
				<el-button class="fullscreen-btn" @click="toggleFullscreen">全屏编辑</el-button>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus/es'
import { templateDetailApi, templateSubmitApi } from '@/api/template'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { Codemirror } from 'vue-codemirror'
import { keymap } from '@codemirror/view'
import { EditorView } from '@codemirror/view'

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
	templateType: null
})

watch(
	//添加模板类型监听（当类型变化时重新校验内容字段）
	() => dataForm.templateType,
	(newVal, _) => {
		if (newVal === 0) {
			dataRules.templateContent = [{ required: dataForm.templateType === 0, message: '必填项不能为空', trigger: 'blur' }]
		} else {
			dataRules.templateContent = []
		}
	}
)

const init = (id?: number) => {
	visible.value = true
	dataForm.id = null

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
		dataForm.templateContent = ''
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

const dataRules = reactive({
	templateName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorPath: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	templateType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

// 新增全屏状态
const isFullScreen = ref(false)
let editorView: EditorView | null = null

// 新增编辑器准备回调
const handleEditorReady = (view: EditorView) => {
	editorView = view
}

// 新增全屏切换方法
const toggleFullscreen = () => {
	isFullScreen.value = !isFullScreen.value
	if (isFullScreen.value && editorView) {
		nextTick(() => editorView?.focus())
	}
}

// 新增对话框关闭处理
const handleDialogClosed = () => {
	isFullScreen.value = false
}

// 修改cmOptions配置
const cmOptions = reactive({
	extensions: [keymap.of([]), EditorView.contentAttributes.of({ autocomplete: 'on' })],
	lineNumbers: true,
	tabSize: 2,
	autocompletion: true
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
}

.code-editor.full-screen {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	z-index: 9999;
	background: white;
}

/* 新增确认按钮样式 */
.confirm-btn {
	position: fixed;
	right: 120px;
	top: 20px;
	z-index: 10000;
	padding: 12px 24px;
}
</style>
