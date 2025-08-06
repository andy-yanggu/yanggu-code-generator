<template>
	<el-dialog v-model="dialogVisible" title="请选择模板、表和枚举" width="85%" @close="dialogVisible = false">
		<el-container>
			<!-- 步骤条 -->
			<el-header height="60px">
				<el-steps :active="activeRef" align-center finish-status="success">
					<el-step title="选择模板"></el-step>
					<el-step title="选择表"></el-step>
					<el-step title="选择枚举"></el-step>
				</el-steps>
			</el-header>
			<!-- 表单区域 -->
			<el-main>
				<template-index v-if="activeRef === 0" ref="templateIndexRef" @select-change="templateSelectChange"></template-index>
				<table-index v-if="activeRef === 1" ref="tableIndexRef" @select-change="tableSelectChange"></table-index>
				<enum-index v-if="activeRef === 2" ref="enumIndexRef" @select-change="enumSelectChange"></enum-index>
			</el-main>
			<!-- 操作按钮 -->
			<el-footer height="60px" style="text-align: center">
				<el-button v-if="activeRef > 0" type="primary" :icon="ArrowLeft" @click="prevStep()">上一步</el-button>
				<el-button v-if="activeRef < 2" type="primary" @click="nextStep()">
					下一步<el-icon class="el-icon--right"><ArrowRight></ArrowRight></el-icon>
				</el-button>
				<el-button v-if="activeRef === 2" :loading="generateCodeLoading" type="success" :icon="DocumentAdd" @click="generateCode()">
					生成代码
				</el-button>
			</el-footer>
		</el-container>
	</el-dialog>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref } from 'vue'
import TemplateIndex from './template-index.vue'
import TableIndex from './table-index.vue'
import EnumIndex from './enum-index.vue'
import { ElMessage } from 'element-plus'
import { generatorProjectDownloadLocalApi, generatorProjectDownloadZipApi } from '@/api/generator'
import { ArrowLeft, ArrowRight, DocumentAdd } from '@element-plus/icons-vue'

const generateCodeLoading = ref(false)
const activeRef = ref(0)
const dialogVisible = ref(false)
const tableIndexRef = ref()
const templateIndexRef = ref()
const enumIndexRef = ref()
const projectReactive = reactive({
	id: null,
	tableTemplateGroupId: null,
	projectTemplateGroupId: null,
	enumTemplateGroupId: null,
	generatorType: null
})
const templateListRef = ref<any[]>([])
const tableListRef = ref<any[]>([])
const enumListRef = ref<any[]>([])

// 修改生成代码方法
const generateCode = () => {
	const dataForm = {
		projectId: projectReactive.id,
		projectTemplateIdList: templateListRef.value.filter(item => item.templateGroupType === 0).map(item => item.id),
		tableTemplateIdList: templateListRef.value.filter(item => item.templateGroupType === 1).map(item => item.id),
		enumTemplateIdList: templateListRef.value.filter(item => item.templateGroupType === 2).map(item => item.id),
		tableIdList: tableListRef.value.map(item => item.id),
		enumIdList: enumListRef.value.map(item => item.id)
	}

	const generatorType = projectReactive.generatorType
	if (generatorType === 0) {
		generateCodeLoading.value = true
		generatorProjectDownloadZipApi(dataForm).then(() => {
			ElMessage.success({
				message: '代码已经下载到浏览器',
				duration: 1000
			})
			dialogVisible.value = false
			generateCodeLoading.value = false
		})
	} else if (generatorType === 1) {
		generateCodeLoading.value = true
		generatorProjectDownloadLocalApi(dataForm).then(() => {
			ElMessage.success({
				message: '代码已经下载到本地',
				duration: 1000
			})
			dialogVisible.value = false
			generateCodeLoading.value = false
		})
	}
}

// 修改初始化方法
const init = (projectItem: any) => {
	activeRef.value = 0
	dialogVisible.value = true
	projectReactive.id = projectItem.id
	projectReactive.tableTemplateGroupId = projectItem.tableTemplateGroupId
	projectReactive.projectTemplateGroupId = projectItem.projectTemplateGroupId
	projectReactive.enumTemplateGroupId = projectItem.enumTemplateGroupId
	projectReactive.generatorType = projectItem.generatorType
	nextTick(() => {
		const templateGroupIdList = [projectReactive.projectTemplateGroupId, projectReactive.tableTemplateGroupId, projectReactive.enumTemplateGroupId]
		templateIndexRef.value.init(templateGroupIdList)
	})
}

//上一步
const prevStep = () => {
	if (activeRef.value > 0) {
		activeRef.value--

		if (activeRef.value === 0) {
			nextTick(() => {
				const templateGroupIdList = [
					projectReactive.projectTemplateGroupId,
					projectReactive.tableTemplateGroupId,
					projectReactive.enumTemplateGroupId
				]
				templateIndexRef.value.init(templateGroupIdList)

				//恢复之前的勾选
				templateIndexRef.value.toggleRowSelection(templateListRef.value)
			})
		}

		if (activeRef.value === 1) {
			nextTick(() => {
				tableIndexRef.value.init(projectReactive.id)

				//恢复之前的勾选
				tableIndexRef.value.toggleRowSelection(tableListRef.value)
			})
		}
	}
}

//下一步
const nextStep = () => {
	if (activeRef.value < 2) {
		activeRef.value++
	}

	if (activeRef.value === 1) {
		nextTick(() => {
			tableIndexRef.value.init(projectReactive.id)

			//恢复之前的勾选
			tableIndexRef.value.toggleRowSelection(tableListRef.value)
		})
	}
	if (activeRef.value === 2) {
		nextTick(() => {
			enumIndexRef.value.init(projectReactive.id)

			//恢复枚举选中状态
			enumIndexRef.value.toggleRowSelection(enumListRef.value)
		})
	}
}

const templateSelectChange = (data: any[]) => {
	templateListRef.value = data
}

const tableSelectChange = (data: any[]) => {
	tableListRef.value = data
}

const enumSelectChange = (data: any[]) => {
	enumListRef.value = data
}

defineExpose({
	init
})
</script>
