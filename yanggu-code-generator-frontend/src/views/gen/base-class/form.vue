<template>
	<el-dialog v-model="visible" :title="dialogTitle" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="基类名称" prop="baseClassName">
				<el-input v-model="state.dataForm.baseClassName" clearable placeholder="请输入基类名称"></el-input>
			</el-form-item>
			<el-form-item label="基类包名" prop="packageName">
				<el-input v-model="state.dataForm.packageName" clearable placeholder="请输入基类包名"></el-input>
			</el-form-item>
			<el-form-item label="基类类名" prop="className">
				<el-input v-model="state.dataForm.className" clearable placeholder="请输入基类类名"></el-input>
			</el-form-item>
			<el-form-item label="基类字段" prop="fields">
				<el-input v-model="state.dataForm.fields" clearable placeholder="请输入基类字段，多个用英文逗号分隔"></el-input>
			</el-form-item>
			<el-form-item label="备注" prop="remark">
				<el-input v-model="state.dataForm.remark" clearable placeholder="请输入备注"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { computed, PropType, reactive } from 'vue'
import { baseClassDetailApi, baseClassSubmitApi } from '@/api/gen/base-class'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenBaseClassForm'
})

// 定义组件props
const props = defineProps({
	mode: {
		type: String as PropType<'add' | 'update' | 'copy'>,
		required: true
	}
})

// 计算对话框标题
const dialogTitle = computed(() => {
	switch (props.mode) {
		case 'add':
			return '新增'
		case 'update':
			return '修改'
		case 'copy':
			return '复制'
		default:
			return '操作'
	}
})

const emit = defineEmits(['refreshDataList'])
const state: FormOptions = reactive({
	// 提交API
	submitApi: baseClassSubmitApi,
	// 详情API
	detailApi: baseClassDetailApi,
	// 表单数据
	dataForm: {
		id: null,
		baseClassName: '',
		packageName: '',
		className: '',
		fields: '',
		remark: ''
	},
	initAfter: () => {
		if (props.mode === 'copy') {
			state.dataForm.baseClassName = state.dataForm.baseClassName + '_复制'
			state.dataForm.id = null
			state.message = '复制成功'
		} else {
			state.message = ''
		}
	},
	emit
})

const dataRules = reactive({
	baseClassName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	packageName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	className: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	fields: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
