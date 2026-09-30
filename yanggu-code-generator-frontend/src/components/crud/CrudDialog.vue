<template>
	<el-dialog v-model="visible" :title="title" :close-on-click-modal="false">
		<el-form ref="formRef" :model="formData" :rules="rules" :label-width="labelWidth" @keyup.enter="handleSubmit">
			<form-field
				v-for="field in formSchema"
				:key="field.prop"
				v-model="formData"
				:field="field"
				:mode="mode"
			>
				<!-- 透传插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
				<template v-for="(_, name) in passthroughSlots" :key="name" #[name]="slotData">
					<slot :name="name" v-bind="slotData ?? {}" />
				</template>
			</form-field>
		</el-form>
		<template #footer>
			<template v-if="mode !== 'detail'">
				<el-button type="primary" :icon="Check" :loading="submitLoading" @click="handleSubmit">确定</el-button>
				<el-button :icon="Close" @click="visible = false">取消</el-button>
			</template>
			<template v-else>
				<el-button :icon="Close" @click="visible = false">关闭</el-button>
			</template>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { Check, Close } from '@element-plus/icons-vue'
import type { FormFieldSchema, FormMode } from '@/types/schema'
import type { FormItemRule } from 'element-plus'
import FormField from './internal/FormField.vue'

defineOptions({
	name: 'CrudDialog'
})

const visible = defineModel<boolean>('visible', { default: false })
const formData = defineModel<Record<string, any>>('formData', { required: true })

const props = withDefaults(
	defineProps<{
		mode: FormMode
		formSchema: FormFieldSchema[]
		rules?: Record<string, FormItemRule[]>
		labelWidth?: string
		subject?: string
		submitLoading?: boolean
	}>(),
	{
		labelWidth: '100px',
		subject: '数据',
		submitLoading: false
	}
)

const emit = defineEmits<{
	(e: 'submit'): void
}>()

const formRef = ref()

// 标题
const titleMap: Record<string, string> = { add: '新增', update: '修改', detail: '' }
const title = computed(() => {
	if (props.mode === 'detail') return `${props.subject}详情`
	return `${titleMap[props.mode] ?? '操作'}${props.subject}`
})

// 提交（内部校验，通过后才 emit）
const handleSubmit = () => {
	formRef.value?.validate((valid: boolean) => {
		if (valid) {
			emit('submit')
		}
	})
}

// 暴露 formRef
defineExpose({ formRef })

// 透传插槽（排除保留插槽，直接转发给 FormField）
const reservedSlotNames = new Set(['toolbar-left', 'action-column'])
const passthroughSlots = computed(() => {
	const slots: Record<string, any> = {}
	for (const name in useSlots()) {
		if (!reservedSlotNames.has(name)) {
			slots[name] = true
		}
	}
	return slots
})
</script>
