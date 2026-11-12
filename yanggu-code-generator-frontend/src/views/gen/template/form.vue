<template>
	<crud-dialog :form-hook="formHook">
		<!-- 路径：树上下文回填，只读展示（DynamicField 无 disabled 透传，走字段插槽） -->
		<template #field-templatePath>
			<el-input v-model="formHook.form.data.templatePath" disabled></el-input>
		</template>

		<!-- 模板类型：由打开入口（右键菜单项）预设，表单内不可改 -->
		<template #field-templateType>
			<el-radio-group v-model="formHook.form.data.templateType" :options="TEMPLATE_TYPES.items" disabled></el-radio-group>
		</template>

		<!-- 二进制文件上传：FileReader 读 base64 进 templateContent，文件名进 binaryOriginalFileName -->
		<template #field-binaryOriginalFileName>
			<el-upload :limit="1" :file-list="fileList" :on-exceed="handleExceed" :http-request="handleManualUpload" :on-remove="handleRemove">
				<el-button type="primary" :icon="Upload">点击上传</el-button>
			</el-upload>
		</template>
	</crud-dialog>
</template>

<script setup lang="ts">
import CrudDialog from '@/components/crud/CrudDialog.vue'
import { genTemplateApi } from '@/api'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { useCrudForm } from '@/hooks'
import { Upload } from '@element-plus/icons-vue'
import type { FormItemRule, UploadProps } from 'element-plus'
import type { FormFieldSchema, FormInitOptions, GenTemplateEntity, Key } from '@/types'

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

// ====== 上下文 ======
// 右键菜单入口决定 parentId / templateType / templatePath（tree.vue 的 contextMenu），
// 连同当前模板组 id 组成 formContext：add 经 initFormData(ctx) 注入，update 在 afterDataAssign 覆盖（旧 initAfterHandle 语义）
const formContext = computed(() => ({
	templateGroupId: props.templateGroupId,
	parentId: props.parentId,
	templateType: props.templateType,
	templatePath: props.templatePath
}))

// ====== 表单字段 ======
// templateType 在弹窗期间恒定（控件禁用），visible 以表单数据为准即可
const formSchema: FormFieldSchema[] = [
	{ key: 'templatePath', label: '路径' },
	// fileName 的两个变体：共享 prop、visible 互斥，label/placeholder 随类型声明式切换。
	// 必填文案不写在 ruleList（mergedRules 按 prop 归并，后声明变体会覆盖前者），由 extraRules 按类型下发
	{ key: 'fileNameDir', prop: 'fileName', label: '目录名称', visible: data => data.templateType === 0 },
	{ key: 'fileNameFile', prop: 'fileName', label: '文件名称', visible: data => data.templateType !== 0 },
	{ key: 'templateType', label: '模板类型', ruleList: [{ required: true, message: '模板类型不能为空', trigger: 'blur' }] },
	{
		key: 'conditionExpression',
		label: '条件表达式',
		tooltip: '使用Aviator表达式，需要返回布尔值',
		visible: data => [0, 1].includes(data.templateType)
	},
	// 隐藏即卸载注销校验，旧 validator 的「type!=0 才必填」条件由 visible 承接
	{ key: 'templateName', label: '模板名称', visible: data => data.templateType === 1, ruleList: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }] },
	{ key: 'binaryOriginalFileName', label: '文件上传', visible: data => data.templateType === 2, ruleList: [{ required: true, message: '文件不能为空', trigger: 'blur' }] },
	{ key: 'templateDesc', label: '描述' }
]

// ====== 动态校验规则 ======
// getter 形态：mergedRules 内 toValue 解包时追踪 form.data.templateType，切换类型（含详情回填）后文案自动重算
const buildExtraRules = (): Record<string, FormItemRule[]> => ({
	fileName: [
		{
			required: true,
			message: `${formHook.form.data.templateType === 0 ? '目录名称' : '文件名称'}不能为空`,
			trigger: 'blur'
		}
	]
})

const fileList = ref([] as File[])

// ====== 表单 hook ======
const formHook = useCrudForm<GenTemplateEntity, GenTemplateEntity>({
	subject: '模板',
	formSchema,
	initFormData: ctx => ({
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
		templatePath: '',
		...ctx
	}),
	submitApi: genTemplateApi.submit,
	detailApi: (id: Key) => genTemplateApi.detailData({ id }),
	formContext,
	extraRules: buildExtraRules,
	labelWidth: '110px',
	dialogWidth: '60%',
	beforeOpen: () => {
		// 每次打开重置文件列表，避免带上上一条二进制文件的显示
		fileList.value = []
	},
	afterDataAssign: () => {
		// 与旧 initAfterHandle 对齐：详情回填后仍以下次入口上下文为准（父级/路径由树节点决定，不信任详情回传）
		const data = formHook.form.data
		Object.assign(data, toValue(formContext))
		if (data.templateType === 2 && data.binaryOriginalFileName) {
			fileList.value = [{ name: data.binaryOriginalFileName } as File]
		}
	},
	afterSubmit: data => emit('refreshDataList', data)
})

// ====== 文件上传（逻辑与旧版一致） ======
// 统一的文件处理方法
const processFile = (file: File) => {
	return new Promise<void>(resolve => {
		const reader = new FileReader()
		reader.onload = e => {
			// 更新表单中的 templateContent 字段
			formHook.form.data.templateContent = e.target?.result as string
			formHook.form.data.binaryOriginalFileName = file.name

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
	formHook.form.data.binaryOriginalFileName = ''
	formHook.form.data.templateContent = ''
	fileList.value = []

	// 处理新文件
	const file = files[0] as File
	processFile(file)
}

// 处理文件移除
const handleRemove: UploadProps['onRemove'] = () => {
	// 清除文件数据
	formHook.form.data.binaryOriginalFileName = ''
	formHook.form.data.templateContent = ''

	// 清空文件列表
	fileList.value = []
}

// ====== 对外契约（与旧版一致，tree.vue 的 useInitForm 继续以 formRef.value.init({ type, id }) 调用） ======
const init = (options: FormInitOptions) => {
	formHook.dialog.open(options.type, options.id)
}

defineExpose({
	init
})
</script>
<style scoped></style>
