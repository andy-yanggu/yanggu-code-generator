<template>
	<div v-loading="currentLoading" class="iframe-container">
		<iframe :src="iframeSrc" width="100%" height="100%" style="min-height: calc(100vh - 84px)" allow="accelerometer" @load="handleLoad"></iframe>
	</div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps({
	iframeSrc: { type: String, required: true },
	loading: { type: Boolean, default: true }, // 仅缓存模式可绑定
	cache: { type: Boolean, default: false } // 是否缓存
})

const emit = defineEmits(['update:loading'])

// 区分缓存和非缓存模式
// 缓存 iframe：currentLoading 由父组件 v-model 控制
// 非缓存 iframe：currentLoading 内部 ref 自己控制
const internalLoading = ref(true)

const currentLoading = computed({
	get() {
		return props.cache ? props.loading : internalLoading.value
	},
	set(val: boolean) {
		if (props.cache) {
			emit('update:loading', val)
		} else {
			internalLoading.value = val
		}
	}
})

const handleLoad = () => {
	currentLoading.value = false
}
</script>

<style scoped>
.iframe-container {
	height: 100%;
	overflow: hidden;
}
</style>
