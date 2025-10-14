<template>
	<el-dialog v-model="visible" :title="dialogTitle" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="120px" @keyup.enter="submitHandle()">
			<el-form-item prop="groupName">
				<template #label>
					<div style="display: flex; align-items: center">
						<span>模板组名称</span>
						<el-tooltip content="模板组名称具有唯一性，不能重复" effect="dark" placement="top">
							<el-icon style="margin-left: 5px; cursor: pointer"><InfoFilled></InfoFilled></el-icon>
						</el-tooltip>
					</div>
				</template>
				<el-input v-model="state.dataForm.groupName" clearable placeholder="请输入模板组名称"></el-input>
			</el-form-item>
			<el-form-item label="模板组类型" prop="type">
				<el-select v-model="state.dataForm.type" :disabled="state.dataForm.id != null" clearable filterable placeholder="请选择模板组类型">
					<el-option v-for="item in TEMPLATE_GROUP_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="模板组描述" prop="groupDesc">
				<el-input v-model="state.dataForm.groupDesc" clearable placeholder="请输入模板组描述"></el-input>
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
import { genTemplateGroupApi } from '@/api/gen/template-group'
import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close, InfoFilled } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenTemplateGroupForm'
})

const emit = defineEmits(['refreshDataList'])

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

const state: FormOptions = reactive({
	// 提交API
	submitApi: genTemplateGroupApi.submit,
	// 详情API
	detailApi: genTemplateGroupApi.detail,
	// 表单数据
	dataForm: {
		id: '',
		groupName: '',
		type: '',
		groupDesc: ''
	},
	initAfter: () => {
		if (props.mode == 'copy') {
			state.submitApi = genTemplateGroupApi.copy
			state.message = '模板组和下的所有模板、属性已复制'
			state.duration = 2000
			state.dataForm.groupName = state.dataForm.groupName + '_复制'
		} else {
			state.submitApi = genTemplateGroupApi.submit
			state.message = ''
			state.duration = 500
		}
	},
	emit
})

const dataRules = reactive({
	groupName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	type: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
