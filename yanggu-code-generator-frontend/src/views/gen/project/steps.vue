<template>
	<el-dialog v-model="dialogVisible" title="请选择模板、表和枚举" width="85%" destroy-on-close @close="dialogVisible = false">
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
		</el-container>
		<!-- 操作按钮 -->
		<template #footer>
			<div style="text-align: center">
				<el-button v-if="activeRef > 0" type="primary" :icon="ArrowLeft" @click="prevStep()">上一步</el-button>
				<el-button v-if="activeRef < 2" type="primary" @click="nextStep()">
					下一步
					<el-icon class="el-icon--right"><ArrowRight></ArrowRight></el-icon>
				</el-button>
				<el-button v-if="activeRef === 2" :loading="submitLoading" type="success" :icon="DocumentAdd" @click="generateCode()">生成</el-button>
			</div>
		</template>
	</el-dialog>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref, shallowReactive } from 'vue'
import TemplateIndex from '@/views/gen/project/template-index.vue'
import TableIndex from '@/views/gen/project/table-index.vue'
import EnumIndex from '@/views/gen/project/enum-index.vue'
import { genGeneratorApi } from '@/api'
import { ArrowLeft, ArrowRight, DocumentAdd } from '@element-plus/icons-vue'
import { useSubmitHandler } from '@/hooks'
import { GenProjectEntity, SubmitOptions } from '@/types'

defineOptions({
	name: 'GenProjectSteps'
})

const activeRef = ref(0)
const dialogVisible = ref(false)
const tableIndexRef = ref()
const templateIndexRef = ref()
const enumIndexRef = ref()
const projectReactive = reactive({
	id: -1,
	tableTemplateGroupId: -1,
	projectTemplateGroupId: -1,
	enumTemplateGroupId: -1,
	generatorType: -1
})
const templateListRef = ref<any[]>([])
const tableListRef = ref<any[]>([])
const enumListRef = ref<any[]>([])

// 初始化方法
const init = (projectItem: GenProjectEntity) => {
	activeRef.value = 0
	dialogVisible.value = true
	projectReactive.id = projectItem.id as number
	projectReactive.tableTemplateGroupId = projectItem.tableTemplateGroupId as number
	projectReactive.projectTemplateGroupId = projectItem.projectTemplateGroupId as number
	projectReactive.enumTemplateGroupId = projectItem.enumTemplateGroupId as number
	projectReactive.generatorType = projectItem.generatorType as number
	templateListRef.value = []
	tableListRef.value = []
	enumListRef.value = []
	nextTick(() => {
		const templateGroupIdList = [projectReactive.projectTemplateGroupId, projectReactive.tableTemplateGroupId, projectReactive.enumTemplateGroupId]
		templateIndexRef.value.init(templateGroupIdList)
	})
}

// 上一步
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

// 下一步
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

// 生成代码
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
		submitState.submitApi = genGeneratorApi.projectDownloadZip
		submitState.successMessage = '代码已经下载到浏览器'
	} else if (generatorType === 1) {
		submitState.submitApi = genGeneratorApi.projectDownloadLocal
		submitState.successMessage = '代码已经生成到本地'
	}
	submitHandle(dataForm)
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

const submitState = shallowReactive({
	visible: dialogVisible,
	successDuration: 1000
} as SubmitOptions)

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

defineExpose({
	init
})
</script>
