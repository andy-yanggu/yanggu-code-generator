<template>
	<el-dialog v-model="dialogVisible" title="请选择模板" width="75%" @close="dialogVisible = false">
		<el-card class="layout-query">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item prop="templateName">
					<el-input v-model="state.queryForm.templateName" clearable placeholder="请输入模板名称"></el-input>
				</el-form-item>
				<el-form-item prop="templateType">
					<el-select v-model="state.queryForm.templateType" style="width: 160px" clearable placeholder="请选择模板类型">
						<el-option v-for="item in TEMPLATE_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button icon="Search" type="primary" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button icon="RefreshRight" @click="resetQueryRef()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card>
			<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="templateName" label="模板名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="generatorPath" label="模板路径" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="templateType" label="模板类型" header-align="center" align="center" :formatter="handlerType"></el-table-column>
				<el-table-column prop="templateDesc" label="模板描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			</el-table>
			<template #footer>
				<div class="footer-buttons">
					<el-button @click="dialogVisible = false">取消</el-button>
					<el-button type="primary" @click="generateCode()">生成代码</el-button>
				</div>
			</template>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { generatorTableDownloadLocalApi, generatorTableDownloadZipApi } from '@/api/generator'
import { ElMessage } from 'element-plus'

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true // 表示父组件必须传递该值
	},
	tableId: {
		type: Number,
		required: true // 表示父组件必须传递该值
	},
	generatorType: {
		type: Number,
		required: true // 表示父组件必须传递该值
	}
})

const state: IHooksOptions = reactive({
	dataListUrl: '/template/entityList',
	deleteUrl: '/template/deleteList',
	isPage: false,
	createdIsNeed: false,
	queryForm: {
		templateGroupId: props.templateGroupId,
		templateName: '',
		templateType: null
	}
})

const dialogVisible = ref(false)
const queryRef = ref()

const init = () => {
	dialogVisible.value = true

	//重置查询表单数据
	if (queryRef.value) {
		resetQueryRef()
	}

	//加载列表数据
	getDataList()
}

const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const handlerType = (row: any) => {
	return TEMPLATE_TYPES.find(item => item.value === row.templateType)?.label
}

const generateCode = () => {
	const data = state.dataListSelections ? state.dataListSelections : []

	if (data.length === 0) {
		ElMessage.warning('请选择模板')
		return
	}
	const dataForm = {
		tableId: props.tableId,
		templateIdList: state.dataListSelections
	}
	const generatorType = props.generatorType
	if (generatorType === 0) {
		generatorTableDownloadZipApi(dataForm)
	} else if (generatorType === 1) {
		generatorTableDownloadLocalApi(dataForm).then(() => {
			ElMessage.success({
				message: '代码已经下载到本地',
				duration: 1000
			})
		})
	}
	dialogVisible.value = false
}

const { getDataList, selectionChangeHandle } = useCrud(state)

defineExpose({
	init
})
</script>

<style scoped>
.footer-buttons {
	display: flex;
	justify-content: flex-end;
}
</style>
