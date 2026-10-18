<template>
	<el-dialog v-model="formHook.dialog.visible" :title="formHook.dialog.title" :close-on-click-modal="false">
		<el-form
			:ref="formHook.form.formRef"
			v-loading="formHook.dialog.detailLoading"
			element-loading-text="数据加载中..."
			:disabled="formHook.dialog.mode === 'detail'"
			:model="formHook.form.data"
			:rules="formHook.form.rules.value"
			:label-width="formHook.form.labelWidth"
			:validate-on-rule-change="false"
			@keyup.enter="handleEnterSubmit"
		>
			<!-- form.data 是父子共享的同一个 reactive 对象：FormField 内部改的是 model[prop]，
			     本质就是改这个对象的属性，从不 emit update:modelValue，所以 v-model 的写回分支是死代码，
			     用单向绑定把「只读、修改走对象属性」这个意图写准确。 -->
			<form-field
				v-for="field in formHook.formSchema"
				:key="field.prop"
				:model-value="formHook.form.data"
				:field="field"
				:mode="formHook.dialog.mode"
			>
				<!-- 透传插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
				<template v-for="(_, name) in passthroughSlots" :key="name" #[name]="slotData">
					<slot :name="name" v-bind="slotData ?? {}"></slot>
				</template>
			</form-field>
		</el-form>
		<template #footer>
			<template v-if="formHook.dialog.mode !== 'detail'">
				<el-button
					type="primary"
					:icon="Check"
					:disabled="formHook.dialog.detailLoading"
					:loading="formHook.submit.loading"
					@click="formHook.submit.execute()"
				>
					确定
				</el-button>
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
import type { UseCrudFormReturn } from '@/types'

defineOptions({
	name: 'CrudDialog'
})

// 接收 hook 返回值作为 prop（纯展示组件，不再内部调用 hook）
// 用具名契约类型而非 any：恢复组件内部对 formHook 的类型检查
const props = defineProps<{
	formHook: UseCrudFormReturn
}>()

/**
 * 回车提交。
 *
 * 不能直接写成 @keyup.enter="formHook.submit.execute()"：那是挂在 el-form 上的全局监听，
 * 任何子控件里的回车都会冒泡上来，而这些控件自己就把回车用掉了：
 * - textarea：回车是换行，在备注里敲回车反而把弹窗提交了；
 * - filterable 的 select / cascader / tree-select：回车是选中当前高亮项；
 * - button：回车本身就是 click，再冒泡提交就重复了一次。
 */
const handleEnterSubmit = (e: KeyboardEvent) => {
	const target = e.target as HTMLElement | null
	if (!target) return
	// 多行文本里回车是换行
	if (target.tagName === 'TEXTAREA' || target.isContentEditable) return
	// 可搜索选择器（tree-select 内部也是 el-select）与按钮自己消费回车
	if (target.closest('.el-select, .el-cascader, button')) return
	props.formHook.submit.execute()
}

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
