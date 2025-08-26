<template>
	<div class="iframe-container">
		<iframe :src="iframeSrc" width="100%" height="100%" style="min-height: calc(100vh - 84px)"></iframe>
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const iframeSrc = ref('')

defineOptions({
	name: 'IframeComponent'
})

watch(
	() => route.meta.externalUrl,
	val => {
		iframeSrc.value = (val as string) || ''
	},
	{ immediate: true }
)
</script>

<style scoped>
.iframe-container {
	height: 100%;
	overflow: hidden;
}
</style>
