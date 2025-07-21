<template>
  <el-container>
    <el-aside :style="{ width: store.isCollapseRef ? '64px' : '200px' }">
      <sidebar></sidebar>
    </el-aside>
    <el-container direction="vertical">
      <el-header>
        <navbar></navbar>
      </el-header>
      <el-main>
        <el-scrollbar>
          <router-view v-slot="{ Component, route }">
            <component :is="Component" :key="route.fullPath" />
          </router-view>
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import sidebar from "@/layout/components/sidebar/index.vue";
import navbar from "@/layout/components/navbar/index.vue";
import { appStore } from '@/store/app-store'

const store = appStore()
</script>

<style scoped>
</style>
