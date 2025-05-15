<template>
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
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { generatorTableDownloadLocalApi, generatorTableDownloadZipApi } from '@/api/generator'
import { ElMessage } from 'element-plus'

const state: IHooksOptions = reactive({
	dataListUrl: '/template/voList',
	isPage: false,
	createdIsNeed: false,
	queryForm: {
		templateGroupIdList: [],
		templateName: '',
		templateType: null
	}
})

const queryRef = ref()

const init = (templateGroupIdList: Array<number>) => {
	state.queryForm.templateGroupIdList = templateGroupIdList

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

const { getDataList, selectionChangeHandle } = useCrud(state)

defineExpose({
	init
})
</script>
