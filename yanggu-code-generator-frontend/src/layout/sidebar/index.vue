<template>
	<!-- 项目logo和文字 -->
	<logo v-if="systemSettingStore.isOpenLogo"></logo>
	<!-- 菜单 -->
	<menu-index></menu-index>
</template>

<script setup lang="ts">
import Logo from '@/layout/sidebar/components/logo.vue'
import MenuIndex from '@/layout/sidebar/components/menu/menu-index.vue'
import { useSystemSettingStore } from '@/store/system-setting-store'

const systemSettingStore = useSystemSettingStore()
</script>
