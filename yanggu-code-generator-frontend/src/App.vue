<template>
	<el-config-provider :locale="zhCn" :size="appStore.layoutSize">
		<router-view></router-view>
	</el-config-provider>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { useAppStore } from '@/store/app-store'

dayjs.locale('zh-cn')

const appStore = useAppStore()
</script>
