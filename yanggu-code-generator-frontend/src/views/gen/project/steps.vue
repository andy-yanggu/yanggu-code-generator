<template>
	<el-dialog
		v-model="dialogVisible"
		:title="`生成代码（${projectReactive.projectName}）`"
		width="85%"
		destroy-on-close
		@close="dialogVisible = false"
	>
		<el-container>
			<!-- 步骤条 -->
			<el-header height="60px">
				<el-steps :active="activeRef" align-center finish-status="success">
					<el-step title="选择模板"></el-step>
					<el-step title="选择表"></el-step>
					<el-step title="选择枚举"></el-step>
					<el-step title="生成代码"></el-step>
				</el-steps>
			</el-header>
			<!-- 表单区域 -->
			<el-main>
				<template-index v-if="activeRef === 0" ref="templateIndexRef" @select-change="templateSelectChange"></template-index>
				<table-index v-if="activeRef === 1" ref="tableIndexRef" @select-change="tableSelectChange"></table-index>
				<enum-index v-if="activeRef === 2" ref="enumIndexRef" @select-change="enumSelectChange"></enum-index>
				<generate-result v-if="activeRef === 3" ref="generateResultRef" v-model:dialog-visible="dialogVisible"></generate-result>
			</el-main>
		</el-container>
		<!-- 操作按钮 -->
		<template #footer>
			<div style="text-align: center">
				<el-button v-if="activeRef > 0 && activeRef !== 3" type="primary" :icon="ArrowLeft" @click="prevStep()">上一步</el-button>
				<el-button v-if="activeRef < 2" type="primary" @click="nextStep()">
					下一步
					<el-icon class="el-icon--right"><ArrowRight></ArrowRight></el-icon>
				</el-button>
				<el-button v-if="activeRef === 2" type="success" :icon="DocumentAdd" @click="generateCode()">生成代码</el-button>
			</div>
		</template>
	</el-dialog>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref } from 'vue'
import TemplateIndex from '@/views/gen/project/template-index.vue'
import TableIndex from '@/views/gen/project/table-index.vue'
import EnumIndex from '@/views/gen/project/enum-index.vue'
import GenerateResult from '@/views/gen/project/generate-result.vue'
import { ArrowLeft, ArrowRight, DocumentAdd } from '@element-plus/icons-vue'
import { GenProjectEntity } from '@/types'

defineOptions({
	name: 'GenProjectSteps'
})

const activeRef = ref(0)
const dialogVisible = ref(false)
const tableIndexRef = ref()
const templateIndexRef = ref()
const enumIndexRef = ref()
const generateResultRef = ref()
const projectReactive = reactive({
	id: -1,
	projectName: '',
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
	projectReactive.projectName = projectItem.projectName
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
	activeRef.value = 3
	const dataForm = {
		projectId: projectReactive.id,
		projectTemplateIdList: templateListRef.value.filter(item => item.templateGroupType === 0).map(item => item.id),
		tableTemplateIdList: templateListRef.value.filter(item => item.templateGroupType === 1).map(item => item.id),
		enumTemplateIdList: templateListRef.value.filter(item => item.templateGroupType === 2).map(item => item.id),
		tableIdList: tableListRef.value.map(item => item.id),
		enumIdList: enumListRef.value.map(item => item.id)
	}
	nextTick(() => {
		generateResultRef.value.init(dataForm, projectReactive.generatorType)
	})
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
