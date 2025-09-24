<template>
	<el-tag :type="enumItem.type" :color="enumItem.color">{{ enumItem.label }}</el-tag>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { EnumItem, EnumValueType, getByValue } from '@/utils/enum'

const props = defineProps({
	value: {
		type: [String, Number, Boolean] as PropType<EnumValueType>,
		required: true
	},
	enumItemList: {
		type: Array as PropType<EnumItem[]>,
		required: true
	}
})

const enumItem = computed(() => {
	const byValue = getByValue(props.value, props.enumItemList)
	if (byValue) {
		return byValue
	} else {
		return {
			label: '未知',
			type: 'info',
			color: ''
		}
	}
})
</script>
