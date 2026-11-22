<template>
	<el-dropdown>
		<div class="user-dropdown">
			<template v-if="userStore.userInfo.avatar">
				<el-avatar :src="userStore.userInfo.avatar" class="user-avatar"></el-avatar>
			</template>
			<template v-else>
				<el-icon class="user-avatar">
					<Avatar></Avatar>
				</el-icon>
			</template>
			<el-text style="color: inherit">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</el-text>
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
import { useUserStore } from '@/store'
import { authApi } from '@/api'
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
				path: '/auth/login',
				query: {
					redirect: encodeURIComponent(route.fullPath || '/')
				}
			})
		})
	})
}
</script>
<style scoped lang="scss">
.user-dropdown {
	display: flex;
	align-items: center;
	cursor: pointer;
	gap: 5px;
}
.user-dropdown:hover {
	color: var(--el-color-primary) !important;
}
.user-dropdown:hover .user-avatar {
	transform: scale(var(--icon-hover-transform-scale)) translateY(var(--icon-hover-transform-translate-y));
}
.user-avatar {
	font-size: 16px;
}
</style>
