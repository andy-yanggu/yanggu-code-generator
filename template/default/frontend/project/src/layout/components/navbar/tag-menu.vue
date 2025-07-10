<template>
  <div class="icon-list">
    <div class="icon-item" @click="emits('refreshTag')">
      <el-icon size="10"><Refresh /></el-icon>
      <span>刷新页面</span>
    </div>
    <div class="icon-item" v-if="props.currentTag.fullPath != '/index'" @click="emits('closeTag')">
      <el-icon size="10"><CloseBold /></el-icon>
      <span @click="">关闭当前</span>
    </div>
    <div class="icon-item" v-if="store.tagLength > 1" @click="emits('closeOtherTags')">
      <el-icon size="10"><CircleClose /></el-icon>
      <span @click="">关闭其他</span>
    </div>
    <div class="icon-item" v-if="props.currentTagIndex > 0" @click="emits('closeLeftTag')">
      <el-icon size="10"><Back /></el-icon>
      <span @click="">关闭左侧</span>
    </div>
    <div class="icon-item" v-if="props.currentTagIndex < (store.tagLength - 1)" @click="emits('closeRightTag')">
      <el-icon size="10"><Right /></el-icon>
      <span @click="">关闭右侧</span>
    </div>
    <div class="icon-item" v-if="store.tagLength > 1" @click="emits('closeAllTags')">
      <el-icon size="10"><Close /></el-icon>
      <span @click="">关闭全部</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Back, CircleClose, Close, CloseBold, Refresh, Right } from '@element-plus/icons-vue'
import { appStore } from '@/store'
import { onMounted, defineEmits, defineProps } from 'vue'

const store = appStore()

const props = defineProps({
  currentTag: {
    type: Object,
    required: true,
  },
  currentTagIndex: {
    type: Number,
    required: true,
  },
})

const emits = defineEmits(['refreshTag', 'closeTag', 'closeAllTags', 'closeOtherTags', 'closeLeftTag', 'closeRightTag'])

onMounted(() => {
  console.log('tag-menu mounted', props, store.tagLength)
})
</script>

<style scoped>
.icon-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 6px;
  width: 70px; /* 设置列表容器的宽度 */
}

.icon-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  width: 100%; /* 让项目占满容器宽度 */
}
</style>
