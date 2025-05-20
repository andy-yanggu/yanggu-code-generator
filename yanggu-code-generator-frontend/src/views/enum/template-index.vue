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
					<el-button type="primary" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button @click="resetQueryRef()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card>
			<el-table
				v-loading="state.dataListLoading"
				:data="state.dataList"
				row-key="id"
				border
				class="layout-table"
				@selection-change="selectionChangeHandle"
			>
				<el-table-column type="selection" reserve-selection header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="templateName" label="模板名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="generatorPath" label="模板路径" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="templateType" label="模板类型" header-align="center" align="center" :formatter="handlerType"></el-table-column>
				<el-table-column prop="templateDesc" label="模板描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			</el-table>
			<el-pagination
				:current-page="state.pageNum"
				:page-sizes="state.pageSizes"
				:page-size="state.pageSize"
				:total="state.total"
				layout="total, sizes, prev, pager, next, jumper"
				@size-change="sizeChangeHandle"
				@current-change="currentChangeHandle"
			>
			</el-pagination>
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
import { generatorEnumDownloadLocalApi, generatorEnumDownloadZipApi } from '@/api/generator'
import { ElMessage } from 'element-plus'

const state: IHooksOptions = reactive({
	dataListUrl: '/template/entityPage',
	createdIsNeed: false,
	queryForm: {
		templateGroupId: null,
		templateName: '',
		templateType: null
	}
})

const initData = reactive({
	enumIdList: [],
	generatorType: -1
})

const dialogVisible = ref(false)
const queryRef = ref()

const init = (enumTemplateGroupId: number, generatorType: number, enumIdList: []) => {
	dialogVisible.value = true
	state.queryForm.templateGroupId = enumTemplateGroupId
	initData.enumIdList = enumIdList
	initData.generatorType = generatorType

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
		templateIdList: state.dataListSelections
	}
	if (initData.enumIdList.length == 1) {
		dataForm.enumId = initData.enumIdList[0]
	} else {
		dataForm.enumIdList = initData.enumIdList
	}
	const generatorType = initData.generatorType
	if (generatorType === 0) {
		generatorEnumDownloadZipApi(dataForm)
	} else if (generatorType === 1) {
		generatorEnumDownloadLocalApi(dataForm).then(() => {
			ElMessage.success({
				message: '代码已经下载到本地',
				duration: 1000
			})
		})
	}
	dialogVisible.value = false
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle } = useCrud(state)

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
