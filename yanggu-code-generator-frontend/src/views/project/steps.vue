<template>
	<el-dialog v-model="dialogVisible" title="请选择表和模板" width="75%" @close="dialogVisible = false">
		<el-container>
			<!-- 步骤条 -->
			<el-header height="60px">
				<el-steps :active="active" finish-status="success">
					<el-step title="选择表"></el-step>
					<el-step title="选择模板"></el-step>
				</el-steps>
			</el-header>
			<!-- 表单区域 -->
			<el-main>
				<table-index v-if="active === 0" ref="tableIndexRef" @select-change="tableSelectChange"></table-index>
				<template-index v-if="active === 1" ref="templateIndexRef" @select-change="templateSelectChange"></template-index>
			</el-main>
			<!-- 操作按钮 -->
			<el-footer height="60px" style="text-align: center">
				<el-button v-if="active === 1" @click="prevStep()">上一步</el-button>
				<el-button v-if="active === 0" :disabled="tableListRef.length === 0" @click="nextStep()">下一步</el-button>
				<el-button v-if="active === 1" :disabled="templateListRef.length === 0" @click="generateCode()">生成代码</el-button>
			</el-footer>
		</el-container>
	</el-dialog>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref } from 'vue'
import TableIndex from './table-index.vue'
import TemplateIndex from './template-index.vue'
import { ElMessage } from 'element-plus'
import { generatorTableDownloadLocalApi, generatorTableDownloadZipApi } from '@/api/generator'

const active = ref(0)
const dialogVisible = ref(false)
const tableIndexRef = ref()
const templateIndexRef = ref()
const projectReactive = reactive({
	id: null,
	tableTemplateGroupId: null,
	projectTemplateGroupId: null
})
const tableListRef = ref<any[]>([])
const templateListRef = ref<any[]>([])

//初始化方法
const init = (projectItem: any) => {
	dialogVisible.value = true
	projectReactive.id = projectItem.id
	projectReactive.tableTemplateGroupId = projectItem.tableTemplateGroupId
	projectReactive.projectTemplateGroupId = projectItem.projectTemplateGroupId
	nextTick(() => {
		tableIndexRef.value.init(projectItem.id)
	})
}

//上一步
const prevStep = () => {
	if (active.value > 0) {
		active.value--
		nextTick(() => {
			tableIndexRef.value.init(projectReactive.id)

			//恢复之前的勾选
			tableListRef.value.forEach(item => {
				tableIndexRef.value.toggleRowSelection(item, true)
			})
		})
	}
}

//下一步
const nextStep = () => {
	if (active.value < 1) {
		active.value++
		nextTick(() => {
			templateIndexRef.value.init([projectReactive.projectTemplateGroupId, projectReactive.tableTemplateGroupId])

			//恢复之前的勾选
			templateListRef.value.forEach(item => {
				templateIndexRef.value.toggleRowSelection(item, true)
			})
		})
	}
}

const generateCode = () => {
	console.log('generateCode')
  // const dataForm = {
  //   templateIdList: state.dataListSelections
  // }
  // if (props.tableId) {
  //   dataForm.tableId = props.tableId
  // }
  // if (props.tableIdList) {
  //   dataForm.tableIdList = props.tableIdList
  // }
  // const generatorType = props.generatorType
  // if (generatorType === 0) {
  //   generatorTableDownloadZipApi(dataForm)
  // } else if (generatorType === 1) {
  //   generatorTableDownloadLocalApi(dataForm).then(() => {
  //     ElMessage.success({
  //       message: '代码已经下载到本地',
  //       duration: 1000
  //     })
  //   })
  // }
}

const tableSelectChange = (data: any[]) => {
	tableListRef.value = data
}

const templateSelectChange = (data: any[]) => {
	templateListRef.value = data
}

defineExpose({
	init
})
</script>
