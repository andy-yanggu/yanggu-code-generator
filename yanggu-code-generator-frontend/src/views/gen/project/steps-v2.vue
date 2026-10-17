<template>
	<el-dialog
		v-model="dialogVisible"
		:title="`生成代码（${projectReactive.projectName}）`"
		width="1200px"
		top="6vh"
		destroy-on-close
		:close-on-click-modal="false"
		@close="dialogVisible = false"
	>
		<el-container class="wizard-container">
			<!-- 左侧步骤导航 -->
			<el-aside width="180px" class="wizard-aside">
				<el-steps :active="activeRef" direction="vertical" finish-status="success" class="wizard-steps">
					<el-step
						v-for="(step, index) in STEPS"
						:key="index"
						:title="step.title"
						:description="step.description"
						:class="{ 'is-clickable': index < activeRef }"
						@click="goToStep(index)"
					></el-step>
				</el-steps>
			</el-aside>
			<!-- 右侧内容区 -->
			<el-main class="wizard-main">
				<el-scrollbar>
					<template-index
						v-if="activeRef === 0"
						ref="templateIndexRef"
						:selected-ids="selectedTemplateIds"
						@select-change="templateSelectChange"
					></template-index>
					<table-index v-if="activeRef === 1" ref="tableIndexRef" :selected-ids="selectedTableIds" @select-change="tableSelectChange"></table-index>
					<enum-index v-if="activeRef === 2" ref="enumIndexRef" :selected-ids="selectedEnumIds" @select-change="enumSelectChange"></enum-index>
					<generate-result-v2
						v-if="activeRef === 3"
						ref="generateResultRef"
						v-model:dialog-visible="dialogVisible"
						v-model:finish="finish"
						@back="goBackFromResult"
					></generate-result-v2>
				</el-scrollbar>
			</el-main>
		</el-container>
		<!-- 底部：已选摘要 + 操作按钮 -->
		<template #footer>
			<div class="wizard-footer">
				<span v-if="activeRef < 3" class="wizard-summary">
					已选: {{ templateListRef.length }} 个模板 · {{ tableListRef.length }} 张表 · {{ enumListRef.length }} 个枚举
					<el-text type="info" size="small" style="margin-left: 8px">（不选则默认全选）</el-text>
				</span>
				<span v-else class="wizard-summary"></span>
				<span class="wizard-actions">
					<el-button v-if="activeRef === 3 && !finish" type="primary" :icon="Back" @click="goBackFromResult()">返回修改</el-button>
					<el-button v-if="activeRef > 0 && activeRef !== 3" type="primary" :icon="ArrowLeft" @click="prevStep()">上一步</el-button>
					<el-button v-if="activeRef < 2" type="primary" @click="nextStep()">
						下一步
						<el-icon class="el-icon--right"><ArrowRight></ArrowRight></el-icon>
					</el-button>
					<el-button v-if="activeRef === 2" type="success" :icon="DocumentAdd" @click="generateCode()">生成代码</el-button>
					<el-button v-if="activeRef === 3" :disabled="!finish" :icon="Close" @click="dialogVisible = false">关闭</el-button>
				</span>
			</div>
		</template>
	</el-dialog>
</template>

<script lang="ts" setup>
import TemplateIndex from '@/views/gen/project/template-index.vue'
import TableIndex from '@/views/gen/project/table-index.vue'
import EnumIndex from '@/views/gen/project/enum-index.vue'
import GenerateResultV2 from '@/views/gen/project/generate-result-v2.vue'
import { ArrowLeft, ArrowRight, Back, Close, DocumentAdd } from '@element-plus/icons-vue'
import { GenProjectEntity } from '@/types'

defineOptions({
	name: 'GenProjectStepsV2'
})

const STEPS = [
	{ title: '选择模板', description: '配置代码模板' },
	{ title: '选择表', description: '关联数据表' },
	{ title: '选择枚举', description: '关联枚举类' },
	{ title: '生成代码', description: '确认并生成' }
]

const activeRef = ref(0)
const dialogVisible = ref(false)
const templateIndexRef = ref()
const tableIndexRef = ref()
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
const finish = ref(false)

// 从存储的行对象中派生 ID 数组，传给子组件用于自动恢复勾选
const selectedTemplateIds = computed(() => templateListRef.value.map((item: any) => item.id))
const selectedTableIds = computed(() => tableListRef.value.map((item: any) => item.id))
const selectedEnumIds = computed(() => enumListRef.value.map((item: any) => item.id))

// 初始化方法
const init = (projectItem: GenProjectEntity) => {
	activeRef.value = 0
	dialogVisible.value = true
	finish.value = false
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

// 步骤切换时自动初始化对应子组件
watch(activeRef, (newVal, oldVal) => {
	if (newVal === oldVal || newVal === 3) return
	nextTick(() => {
		if (newVal === 0) {
			const templateGroupIdList = [projectReactive.projectTemplateGroupId, projectReactive.tableTemplateGroupId, projectReactive.enumTemplateGroupId]
			templateIndexRef.value?.init(templateGroupIdList)
		} else if (newVal === 1) {
			tableIndexRef.value?.init(projectReactive.id)
		} else if (newVal === 2) {
			enumIndexRef.value?.init(projectReactive.id)
		}
	})
})

// 点击已完成步骤回跳
const goToStep = (index: number) => {
	if (index < activeRef.value && index < 3) {
		activeRef.value = index
	}
}

// 从结果页返回修改（回到步骤 2）
const goBackFromResult = () => {
	finish.value = false
	activeRef.value = 2
}

// 上一步
const prevStep = () => {
	if (activeRef.value > 0) {
		activeRef.value--
	}
}

// 下一步
const nextStep = () => {
	if (activeRef.value < 2) {
		activeRef.value++
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

<style lang="scss" scoped>
.wizard-container {
	height: 60vh;
}

.wizard-aside {
	overflow-y: auto;
	border-right: 1px solid var(--el-border-color-lighter);
	padding: 10px 0;
}

.wizard-steps {
	padding: 0 15px;

	:deep(.el-step__head) {
		cursor: default;
	}

	:deep(.is-clickable .el-step__head) {
		cursor: pointer;

		&:hover .el-step__icon {
			border-color: var(--el-color-primary);
			color: var(--el-color-primary);
		}

		&:hover .el-step__title {
			color: var(--el-color-primary);
		}
	}
}

.wizard-main {
	overflow-y: auto;
	padding: var(--dialog-body-padding);
}

.wizard-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}

.wizard-summary {
	color: var(--el-text-color-secondary);
	font-size: 13px;
}

.wizard-actions {
	display: flex;
	gap: 8px;
}
</style>
