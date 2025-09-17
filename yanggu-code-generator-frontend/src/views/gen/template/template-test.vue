<template>
	<el-drawer v-model="testData.visible" title="模板测试" size="100%" :modal="false" class="drawer-wrapper">
		<el-container class="full-height">
			<!-- 左侧：选择面板（独立滚动） -->
			<el-aside class="aside-scroll">
				<el-text>请选择项目、表或者枚举进行测试</el-text>
				<el-cascader
					v-model="testData.cascaderValue"
					:options="testData.cascaderData"
					filterable
					placeholder="请选择项目、表或者枚举进行测试"
				></el-cascader>
			</el-aside>

			<!-- 右侧：内容区 -->
			<el-container direction="vertical" class="right-container">
				<!-- 顶部工具栏（固定） -->
				<el-header class="header-fixed">
					<el-row>
						<el-col :span="22">
							<el-tooltip :content="testData.fullFilePath" placement="top">
								<el-text style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; width: 100%">
									路径：{{ testData.fullFilePath }}
								</el-text>
							</el-tooltip>
						</el-col>
						<el-col :span="2" style="text-align: right">
							<el-button
								v-if="testData.activeName === 'original'"
								size="small"
								type="primary"
								:icon="Edit"
								:loading="submitLoading"
								:disabled="testData.editTemplateContent === testData.originalTemplateContent"
								@click="saveTemplateContent()"
							>
								保存
							</el-button>
							<el-button v-else size="small" :icon="Refresh">刷新</el-button>
						</el-col>
					</el-row>

					<el-tabs v-model="testData.activeName">
						<el-tab-pane name="original">
							<template #label>
								<div class="tab-label">
									<span>原始模板</span>
									<span v-if="testData.editTemplateContent !== testData.originalTemplateContent" class="edit-dot"></span>
								</div>
							</template>
						</el-tab-pane>
						<el-tab-pane name="render" label="渲染结果"></el-tab-pane>
					</el-tabs>
				</el-header>

				<!-- 主体内容（独立滚动） -->
				<el-main class="main-scroll">
					<el-scrollbar v-if="testData.activeName === 'original'">
						<code-mirror v-model="testData.editTemplateContent"></code-mirror>
					</el-scrollbar>
					<el-scrollbar v-else>
						<code-mirror v-model="testData.renderedTemplateContent" :read-only="true"></code-mirror>
					</el-scrollbar>
				</el-main>
			</el-container>
		</el-container>
	</el-drawer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import CodeMirror from '@/components/code-mirror/index.vue'
import { templateDetailApi, templateUpdateContentApi } from '@/api/gen/template'
import { getProjectIdListByTemplateGroup } from '@/api/gen/template-group'
import { ElMessage } from 'element-plus'
import { Edit, Refresh } from '@element-plus/icons-vue'

interface CascaderData {
	value: string
	label: string
	children?: CascaderData[]
}

const testData = reactive({
	visible: false,
	activeName: 'original',
	templateGroupId: 0,
	templateGroupType: 0,
	templateId: 0,
	originalFileName: '',
	originalTemplateContent: '',
	editTemplateContent: '',
	fullFilePath: '',
	resultFileName: '',
	renderedTemplateContent: '',
	projectIdList: [],
	cascaderValue: '',
	cascaderData: [] as CascaderData[]
})

const init = async (templateGroupId: number, templateGroupType: number, templateId: number) => {
	testData.visible = true
	testData.templateGroupId = templateGroupId
	testData.templateGroupType = templateGroupType
	testData.templateId = templateId
	testData.activeName = 'original'

	// 获取项目ID列表
	const res = await getProjectIdListByTemplateGroup({ templateGroupType, templateGroupId })
	if (!res.data.length) {
		ElMessage.warning('请先关联项目，若未创建项目，请先创建项目后与当前模板组关联')
		return
	}
	testData.projectIdList = res.data

	// 获取模板详情
	const detailQueryForm = { id: templateId, setPath: true }
	templateDetailApi(detailQueryForm).then(res => {
		testData.originalFileName = res.data.fileName
		testData.originalTemplateContent = res.data.templateContent
		testData.editTemplateContent = res.data.templateContent
		testData.fullFilePath = res.data.generatorPath
	})
}

const submitLoading = ref(false)

// 保存模板内容
const saveTemplateContent = () => {
	submitLoading.value = true
	const dataForm = {
		id: testData.templateId,
		templateContent: testData.editTemplateContent
	}
	templateUpdateContentApi(dataForm)
		.then(() => {
			ElMessage.success({
				message: '保存成功',
				duration: 500
			})
			testData.originalTemplateContent = testData.editTemplateContent
		})
		.finally(() => {
			submitLoading.value = false
		})
}

defineExpose({ init })
</script>

<style scoped>
.full-height {
	height: 100%;
}

.drawer-wrapper {
	--el-drawer-padding-primary: 0; /* 去除默认padding */
}

.aside-scroll {
	height: 100%;
	overflow: auto;
	border-right: 1px solid #e5e5e5;
	padding: 16px;
	box-sizing: border-box;
}

.right-container {
	display: flex;
	flex-direction: column;
	height: 100%;
}

.header-fixed {
	height: auto;
	flex-shrink: 0;
}

.main-scroll {
	flex: 1;
	overflow: auto;
	padding: 0 16px 16px;
}
.tab-label {
	position: relative;
	display: inline-block;
}

/* 仅在包含编辑红点时添加右边距 */
.tab-label:has(.edit-dot) {
	padding-right: 10px;
}

/* 通用红点 */
.edit-dot {
	position: absolute;
	top: 2px;
	right: 0;
	width: 6px;
	height: 6px;
	background-color: #f56c6c;
	border-radius: 50%;
	display: inline-block;
}
</style>
