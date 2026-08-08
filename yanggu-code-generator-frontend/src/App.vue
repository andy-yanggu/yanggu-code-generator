<template>
	<el-config-provider :locale="zhCn" :size="appStore.layoutSize">
		<router-view></router-view>
	</el-config-provider>
</template>

<script setup lang="ts">
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { useAppStore } from '@/store/app-store'

dayjs.locale('zh-cn')

const appStore = useAppStore()
</script>
