<template>
	<icon-button :el-icon="appStore.isCollapse ? Expand : Fold" size="18px" @click="appStore.toggleCollapse()"></icon-button>
</template>
<script setup lang="ts">
import IconButton from '@/components/icon-button/index.vue'
import { useAppStore } from '@/store'
import { Expand, Fold } from '@element-plus/icons-vue'

defineOptions({
	name: 'ToggleCollapse'
})

const appStore = useAppStore()
</script>

<style scoped></style>
