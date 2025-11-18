<template>
	<el-dropdown>
		<div style="display: flex; align-items: center; cursor: pointer; gap: 5px">
			<template v-if="userStore.userInfo.avatar">
				<el-avatar :src="userStore.userInfo.avatar" :size="16"></el-avatar>
			</template>
			<template v-else>
				<el-icon size="16px">
					<Avatar></Avatar>
				</el-icon>
			</template>
			<el-text>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</el-text>
		</div>
		<template #dropdown>
			<el-dropdown-menu>
				<router-link to="/user/profile">
					<el-dropdown-item :icon="User">个人信息</el-dropdown-item>
				</router-link>
				<el-dropdown-item :icon="SwitchButton" @click="logout()">退出登录</el-dropdown-item>
			</el-dropdown-menu>
		</template>
	</el-dropdown>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user-store'
import { authApi } from '@/api/auth'
import { ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { Avatar, SwitchButton, User } from '@element-plus/icons-vue'

defineOptions({
	name: 'UserDropdown'
})

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 退出登录
const logout = () => {
	ElMessageBox.confirm('确定注销并退出系统吗？', '系统提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	}).then(() => {
		authApi.logout().then(() => {
			userStore.clearAll()
			router.push({
				path: '/login',
				query: {
					redirect: encodeURIComponent(route.fullPath || '/')
				}
			})
		})
	})
}
</script>
<style scoped lang="scss"></style>
