<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false">
		<el-form
			ref="dataFormRef"
			:model="state.dataForm"
			:disabled="formType === 'detail'"
			:rules="dataRules"
			label-width="130px"
			:validate-on-rule-change="false"
		>
			<el-form-item prop="baseClassName">
				<template #label>
					<form-label-tooltip label="基类名称" tooltip="基类名称具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.baseClassName" clearable placeholder="请输入基类名称"></el-input>
			</el-form-item>
			<el-form-item prop="fullClassName">
				<template #label>
					<form-label-tooltip label="基类全类名" tooltip="基类全类名具有唯一性，不能重复"></form-label-tooltip>
				</template>
				<div class="full-class-name">
					<el-form-item prop="packageName" class="inner-form-item">
						<el-input v-model="state.dataForm.packageName" style="flex: 1" placeholder="请输入基类包名" clearable></el-input>
					</el-form-item>
					<span class="dot">.</span>
					<el-form-item prop="className" class="inner-form-item">
						<el-input v-model="state.dataForm.className" style="flex: 1" placeholder="请输入基类类名" clearable></el-input>
					</el-form-item>
				</div>
			</el-form-item>
			<el-form-item label="基类字段" prop="fieldList">
				<el-input-tag
					v-model="state.dataForm.fieldList"
					collapse-tags
					collapse-tags-tooltip
					:max-collapse-tags="5"
					draggable
					clearable
					placeholder="请输入基类字段"
				></el-input-tag>
			</el-form-item>
			<el-form-item label="备注" prop="remark">
				<el-input v-model="state.dataForm.remark" clearable placeholder="请输入备注"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button v-if="formType === 'detail'" type="primary" :icon="Edit" @click="formType = 'update'">修改</el-button>
			<el-button v-else type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { genBaseClassApi } from '@/api'
import { FormOptions, GenBaseClassEntity } from '@/types'
import { useSubmitForm } from '@/hooks'
import { Check, Close, Edit } from '@element-plus/icons-vue'
import { FormItemRule } from 'element-plus'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'GenBaseClassForm'
})

// 初始化表单数据
const initFormData = (): GenBaseClassEntity => ({
	id: '',
	baseClassName: '',
	packageName: '',
	className: '',
	fieldList: [] as string[],
	remark: ''
})

const emit = defineEmits(['refreshDataList'])
const state = reactive({
	// 标题主体
	titleSubject: '基类',
	// 提交API
	submitApi: genBaseClassApi.submit,
	// 详情API
	detailApi: genBaseClassApi.detail,
	initFormData,
	dataForm: initFormData(),
	initAfter: () => {
		if (formType.value === 'copy') {
			state.dataForm.baseClassName = state.dataForm.baseClassName + '_复制'
			state.dataForm.id = ''
		}
	},
	emit
} as FormOptions<GenBaseClassEntity>)

const dataRules = computed(() => {
	const rules: Record<keyof GenBaseClassEntity | string, FormItemRule[]> = {}
	if (formType.value === 'detail') {
		return rules
	}
	const constRules: Record<keyof GenBaseClassEntity | string, FormItemRule[]> = {
		baseClassName: [{ required: true, message: '基类名称不能为空', trigger: 'blur' }],
		fullClassName: [{ required: true, validator: (_: any, __: any, callback: any) => callback(), message: '必填项不能为空', trigger: 'blur' }],
		packageName: [{ required: true, message: '基类包名不能为空', trigger: 'blur' }],
		className: [{ required: true, message: '基类类名不能为空', trigger: 'blur' }],
		fieldList: [{ required: true, message: '基类字段不能为空', trigger: 'blur' }]
	}
	Object.assign(rules, constRules)
	return rules
})

const { visible, dataFormRef, formType, dialogTitle, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
<style lang="scss" scoped>
.full-class-name {
	display: flex;
	align-items: center;
	width: 100%;
	gap: 8px;
}

/* 子 form-item 去掉自身的布局副作用 */
.inner-form-item {
	flex: 1;
	margin-bottom: 0; /* 去掉多余行距 */
}

.inner-form-item .el-form-item__content {
	margin-left: 0 !important; /* 去掉 label 占位 */
}

.dot {
	flex-shrink: 0;
}
</style>
