<template>
	<el-tag v-if="isNotBlank(value)" :type="(enumItem as any)?.type" :color="(enumItem as any)?.color">{{ enumItem?.label }}</el-tag>
</template>

<script setup lang="ts">
import type { AnyEnum } from 'enum-plus'
import { isNotBlank } from '@/utils/tool'

defineOptions({
	name: 'DictTag'
})

const props = defineProps({
	value: {
		type: [String, Number, Boolean] as PropType<string | number | boolean | null>,
		default: null,
		required: false
	},
	enumObj: {
		type: Object as PropType<AnyEnum>,
		required: true
	}
})

const enumItem = computed(() => {
	const item = props.enumObj.item(props.value)
	if (item) {
		return item
	} else {
		return {
			label: '未知',
			type: 'info',
			color: ''
		}
	}
})
</script>
