<template>
	<div></div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { onMounted } from 'vue'

const route = useRoute()
const router = useRouter()
const { params, query } = route
const { path } = params

onMounted(() => {
	// console.log('redirect mounted', route, router)
})

defineOptions({
	name: 'Redirect'
})
// query删除t字段
// delete query.t
// console.log('redirect', path, query)
router.replace({ path: '/' + path, query })
</script>
