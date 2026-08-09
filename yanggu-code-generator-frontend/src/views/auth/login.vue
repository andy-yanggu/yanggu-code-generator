<template>
	<el-card class="login">
		<el-form ref="loginRef" :model="form" :rules="dataRules" class="login-form" @keyup.enter="loginHandle()">
			<el-form-item prop="username">
				<el-input v-model="form.username" :prefix-icon="User" clearable placeholder="用户名"></el-input>
			</el-form-item>
			<el-form-item prop="password">
				<el-input v-model="form.password" :prefix-icon="Lock" clearable placeholder="密码" type="password"></el-input>
			</el-form-item>
			<el-form-item style="width: 100%">
				<el-button type="primary" @click="loginHandle()">登录</el-button>
			</el-form-item>
		</el-form>
	</el-card>
</template>

<script setup lang="ts">
import { authApi } from '@/api'
import { useUserStore } from '@/store'
import { Lock, User } from '@element-plus/icons-vue'

defineOptions({
	name: 'AuthLogin'
})

const redirect = ref('/')
const route = useRoute()
const router = useRouter()
const loginRef = ref()

watch(
	() => route,
	() => {
		if (route.query?.redirect) {
			const redirectPath = route.query?.redirect as string
			redirect.value = decodeURIComponent(redirectPath)
		}
	},
	{ immediate: true }
)

const userStore = useUserStore()

const form = reactive({
	username: '',
	password: ''
})

const dataRules = reactive({
	username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
	password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
})

const loginHandle = () => {
	loginRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}
		authApi.login(form).then(data => {
			userStore.setData(data)
			ElMessage.success('登录成功')
			router.push(redirect.value)
		})
	})
}
</script>

<style lang="scss" scoped>
.login {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 100%;
	background-size: cover;
}
.login-form {
	border-radius: 6px;
	background: #ffffff;
	width: 400px;
	padding: 25px 25px 5px 25px;
	z-index: 1;
	.el-input {
		height: 40px;
		input {
			height: 40px;
		}
	}

	.input-icon {
		height: 39px;
		width: 14px;
		margin-left: 0;
	}
}
</style>
