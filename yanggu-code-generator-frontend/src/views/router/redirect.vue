<template>
	<div></div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const { params, query } = route
const { path } = params

defineOptions({
	name: 'Redirect'
})
router.replace({ path: '/' + path, query })
</script>
