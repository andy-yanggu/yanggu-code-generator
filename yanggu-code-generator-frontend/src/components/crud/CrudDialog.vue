<template>
	<el-dialog v-model="formHook.dialog.visible" :title="formHook.dialog.title" :close-on-click-modal="false">
		<el-form
			:ref="formHook.form.formRef"
			:model="formHook.form.data"
			:rules="formHook.form.rules.value"
			:label-width="formHook.form.labelWidth"
			@keyup.enter="formHook.submit.execute()"
		>
			<form-field v-for="field in formHook.formSchema" :key="field.prop" v-model="formHook.form.data" :field="field" :mode="formHook.dialog.mode">
				<!-- 透传插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
				<template v-for="(_, name) in passthroughSlots" :key="name" #[name]="slotData">
					<slot :name="name" v-bind="slotData ?? {}"></slot>
				</template>
			</form-field>
		</el-form>
		<template #footer>
			<template v-if="formHook.dialog.mode !== 'detail'">
				<el-button type="primary" :icon="Check" :loading="formHook.submit.loading" @click="formHook.submit.execute()">确定</el-button>
				<el-button :icon="Close" @click="formHook.dialog.close()">取消</el-button>
			</template>
			<template v-else>
				<el-button :icon="Close" @click="formHook.dialog.close()">关闭</el-button>
			</template>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { Check, Close } from '@element-plus/icons-vue'
import FormField from './internal/FormField.vue'

defineOptions({
	name: 'CrudDialog'
})

// 接收 hook 返回值作为 prop（纯展示组件，不再内部调用 hook）
// 用 any 避免泛型 ReturnType 与页面具体实体类型之间的 TS2719 冲突
const props = defineProps<{
	formHook: any
}>()

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
	open: props.formHook.dialog.open,
	close: props.formHook.dialog.close
})
</script>
