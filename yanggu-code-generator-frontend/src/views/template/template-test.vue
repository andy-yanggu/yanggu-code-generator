<template>
	<el-drawer v-model="testData.visible" title="模板测试" :size="'100%'" :modal="false">
		<el-container>
			<el-header>
				<h1>Header</h1>
			</el-header>
			<el-main>
				<h1>Main</h1>
				<el-tabs v-model="testData.activeName">
					<el-tab-pane name="1" :label="`原始：${testData.originalFileName}`">
						<code-mirror v-model="testData.originalTemplateContent" :read-only="true"></code-mirror>
					</el-tab-pane>
					<el-tab-pane name="2" :label="`渲染：${testData.resultFileName}`">
						<code-mirror v-model="testData.renderedTemplateContent" :read-only="true"></code-mirror>
					</el-tab-pane>
				</el-tabs>
			</el-main>
		</el-container>
	</el-drawer>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import CodeMirror from '@/components/code-mirror/index.vue'
import { templateDetailApi } from '@/api/template'

const testData = reactive({
	visible: false,
	activeName: '1',
	templateGroupId: 0,
	templateGroupType: 0,
	templateId: 0,
	originalFileName: '',
	originalTemplateContent: '',
	resultFileName: '',
	renderedTemplateContent: ''
})

const init = (templateGroupId: number, templateGroupType: number, templateId: number) => {
	testData.visible = true
	testData.templateGroupId = templateGroupId
	testData.templateGroupType = templateGroupType
	testData.templateId = templateId
	testData.activeName = '1'

	// 调用后端接口，获取模板信息
	templateDetailApi(templateId).then(res => {
		testData.originalFileName = res.data.fileName
		testData.originalTemplateContent = res.data.templateContent
	})
}

defineExpose({
	init
})
</script>

<style scoped></style>
