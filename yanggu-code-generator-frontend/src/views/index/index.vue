<template>
	<div v-html="readmeContent"></div>
</template>

<script setup lang="ts">
import README from './README.html?raw'
import { ref, onMounted } from 'vue'

const readmeContent = ref('')

onMounted(() => {
	readmeContent.value = README
})
</script>

<style scoped></style>
