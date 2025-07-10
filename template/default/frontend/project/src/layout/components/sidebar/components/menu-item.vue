<template>
  <el-sub-menu
    v-if="menu.children?.length > 0"
    :key="menu.path"
    :index="menu.path"
  >
    <template #title>
      <el-icon>
        <component :is="menu.meta.icon"></component>
      </el-icon>
      <span>{{ menu.meta.title }}</span>
    </template>
    <menu-item
      v-for="sub in menu.children"
      :key="sub.path"
      :menu="sub"
    ></menu-item>
  </el-sub-menu>
  <el-menu-item v-else :key="menu.path" :index="menu.path">
    <el-icon>
      <component :is="menu.meta.icon"></component>
    </el-icon>
    <span>{{ menu.meta.title }}</span>
  </el-menu-item>
</template>

<script setup lang="ts">
defineProps({
  menu: {
    type: Object,
    required: true,
  },
});
</script>
