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
				<table-index v-if="active === 0" ref="tableIndexRef"></table-index>
				<template-index v-if="active === 1" ref="templateIndexRef"></template-index>
			</el-main>
			<!-- 操作按钮 -->
			<el-footer height="60px" style="text-align: center">
				<el-button :disabled="active === 0" @click="prevStep">上一步</el-button>
				<el-button type="primary" @click="nextStep">
					{{ active === 1 ? '完成' : '下一步' }}
				</el-button>
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
const tableIdListRef = ref([])

const nextStep = () => {
	if (active.value < 1) {
		const tableIdList = tableIndexRef.value.getSelections()
		if (tableIdList.length === 0) {
			ElMessage.warning('请选择生成的表')
			return
		}
		tableIdListRef.value = tableIdList
		active.value++
		nextTick(() => {
			templateIndexRef.value.init([projectReactive.projectTemplateGroupId, projectReactive.tableTemplateGroupId])
		})
	}
}

const prevStep = () => {
	if (active.value > 0) {
		active.value--
	}
}

const init = (projectItem: any) => {
	dialogVisible.value = true
	projectReactive.id = projectItem.id
	projectReactive.tableTemplateGroupId = projectItem.tableTemplateGroupId
	projectReactive.projectTemplateGroupId = projectItem.projectTemplateGroupId
	nextTick(() => {
		tableIndexRef.value.init(projectItem.id)
	})
}

// const generateCode = () => {
//   const data = state.dataListSelections ? state.dataListSelections : []
//
//   if (data.length === 0) {
//     ElMessage.warning('请选择模板')
//     return
//   }
//   const dataForm = {
//     templateIdList: state.dataListSelections
//   }
//   if (props.tableId) {
//     dataForm.tableId = props.tableId
//   }
//   if (props.tableIdList) {
//     dataForm.tableIdList = props.tableIdList
//   }
//   const generatorType = props.generatorType
//   if (generatorType === 0) {
//     generatorTableDownloadZipApi(dataForm)
//   } else if (generatorType === 1) {
//     generatorTableDownloadLocalApi(dataForm).then(() => {
//       ElMessage.success({
//         message: '代码已经下载到本地',
//         duration: 1000
//       })
//     })
//   }
// }

defineExpose({
	init
})
</script>
