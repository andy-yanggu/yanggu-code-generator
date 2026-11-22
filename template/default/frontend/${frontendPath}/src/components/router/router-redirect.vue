<template>
	<div></div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

defineOptions({
	name: 'RouterRedirect'
})

const route = useRoute()
const router = useRouter()
const { params, query } = route
const { path } = params

router.replace({ path: '/' + path, query })
</script>
