<template>
	<iframe :src="readmeUrl" class="readme-iframe"></iframe>
</template>

<script setup lang="ts">
import README_URL from './README.html?url'

defineOptions({
	name: 'Index'
})

const readmeUrl = README_URL
</script>

<style scoped>
.readme-iframe {
	width: 100%;
	height: calc(100vh - 100px);
	border: none;
}
</style>
