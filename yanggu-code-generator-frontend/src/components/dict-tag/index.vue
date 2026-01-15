<template>
	<el-tag v-if="isNotBlank(value)" :type="enumItem.type" :color="enumItem.color">{{ enumItem.label }}</el-tag>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { getEnumByValue } from '@/utils/enum'
import { EnumItem, EnumValueType } from '@/types'
import { isNotBlank } from '@/utils/tool'

defineOptions({
	name: 'DictTag'
})

const props = defineProps({
	value: {
		type: [String, Number, Boolean] as PropType<EnumValueType>,
		default: null,
		required: true
	},
	enumItemList: {
		type: Array as PropType<EnumItem[]>,
		required: true
	}
})

const enumItem = computed(() => {
	const byValue = getEnumByValue(props.value, props.enumItemList)
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
