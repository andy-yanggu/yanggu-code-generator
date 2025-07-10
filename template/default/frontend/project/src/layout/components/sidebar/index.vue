<template>
  <div>
    <el-avatar src="./vite.svg" size="small" style="width: 25px; height: 25px; position: relative; top: 5px; margin-left: 18px; margin-right: 5px"></el-avatar>
    <el-text v-if="!store.isCollapseRef"><b>Code Generator</b></el-text>
  </div>
  <Menu></Menu>
</template>

<script setup lang="ts">
import Menu from '@/layout/components/sidebar/components/menu.vue'
import { appStore } from '@/store'

const store = appStore()
</script>
