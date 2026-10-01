<template>
	<el-dialog v-model="formHooks.dialog.visible" :title="formHooks.dialog.title" :close-on-click-modal="false">
		<el-form
			:ref="formHooks.form.formRef"
			:model="formHooks.form.data"
			:rules="formHooks.form.rules.value"
			:label-width="formHooks.form.labelWidth"
			@keyup.enter="formHooks.submit.execute()"
		>
			<form-field v-for="field in formSchema" :key="field.prop" v-model="formHooks.form.data" :field="field" :mode="formHooks.dialog.mode">
				<!-- 透传插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
				<template v-for="(_, name) in passthroughSlots" :key="name" #[name]="slotData">
					<slot :name="name" v-bind="slotData ?? {}"></slot>
				</template>
			</form-field>
		</el-form>
		<template #footer>
			<template v-if="formHooks.dialog.mode !== 'detail'">
				<el-button type="primary" :icon="Check" :loading="formHooks.submit.loading" @click="formHooks.submit.execute()">确定</el-button>
				<el-button :icon="Close" @click="formHooks.dialog.close()">取消</el-button>
			</template>
			<template v-else>
				<el-button :icon="Close" @click="formHooks.dialog.close()">关闭</el-button>
			</template>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { Check, Close } from '@element-plus/icons-vue'
import FormField from './internal/FormField.vue'
import { useCrudForm } from '@/hooks/use-crud-form'
import type { UseCrudFormOptions } from '@/types/hooks/use-crud-form'

defineOptions({
	name: 'CrudDialog'
})

const props = withDefaults(defineProps<UseCrudFormOptions>(), {
	subject: '数据',
	labelWidth: '100px'
})

// 内部创建表单 hook（直接传 props，hook 内部处理 mergedRules）
const formHooks = useCrudForm(props)

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
// 暴露 dialog 方法供父组件调用
defineExpose({
	open: formHooks.dialog.open,
	close: formHooks.dialog.close
})
</script>
