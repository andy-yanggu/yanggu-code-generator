<template>
	<el-card class="layout-query" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-row>
				<el-col :span="8">
					<el-form-item label="模板组名称" prop="templateGroupName">
						<el-input v-model="state.queryForm.templateGroupName" style="width: 140px" clearable placeholder="请输入模板组名称"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="8">
					<el-form-item label="模板组类型" prop="templateGroupType">
						<el-select v-model="state.queryForm.templateGroupType" style="width: 160px" clearable placeholder="请选择模板组类型">
							<el-option v-for="item in TEMPLATE_GROUP_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
						</el-select>
					</el-form-item>
				</el-col>
				<el-col :span="8">
					<el-form-item label="模板名称" prop="templateName">
						<el-input v-model="state.queryForm.templateName" style="width: 140px" clearable placeholder="请输入模板名称"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="8">
					<el-form-item label="模板类型" prop="templateType">
						<el-select v-model="state.queryForm.templateType" style="width: 150px" clearable placeholder="请选择模板类型">
							<el-option v-for="item in TEMPLATE_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
						</el-select>
					</el-form-item>
				</el-col>
				<el-col :span="16">
					<el-form-item>
						<el-button type="primary" :icon="Search" @click="getDataList()">查询</el-button>
					</el-form-item>
					<el-form-item>
						<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
					</el-form-item>
				</el-col>
			</el-row>
		</el-form>
	</el-card>
	<el-card shadow="hover">
		<el-table
			ref="tableRef"
			v-loading="state.dataListLoading"
			row-key="id"
			:data="state.dataList"
			border
			class="layout-table"
			@selection-change="selectionChangeHandle"
		>
			<el-table-column type="selection" reserve-selection header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="templateGroupName" label="模板组名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>"
			<el-table-column
				prop="templateGroupType"
				label="模板组类型"
				show-overflow-tooltip
				header-align="center"
				align="center"
				:formatter="(_: any, __: any, value: any) => getLabel(value, TEMPLATE_GROUP_TYPES)"
			></el-table-column>
			<el-table-column prop="templateName" label="模板名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="templateType"
				label="模板类型"
				header-align="center"
				align="center"
				:formatter="(_: any, __: any, value: any) => getLabel(value, TEMPLATE_TYPES)"
			></el-table-column>
			<el-table-column prop="generatorPath" label="模板路径" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
	</el-card>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { reactive, ref } from 'vue'
import { TEMPLATE_GROUP_TYPES, TEMPLATE_TYPES } from '@/constant/enum'
import { templateVOPageApi } from '@/api/template'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getLabel } from '@/utils/enum'

const emit = defineEmits(['selectChange'])
const tableRef = ref()
const state: IHooksOptions = reactive({
	dataListApi: templateVOPageApi,
	createdIsNeed: false,
	queryForm: {
		templateGroupIdList: [],
		templateGroupName: '',
		templateName: '',
		templateGroupType: null,
		templateType: null
	}
})
let isManualSelection = true

const init = (templateGroupIdList: Array<number>) => {
	state.queryForm.templateGroupIdList = templateGroupIdList

	//重置查询表单数据
	resetQueryHandle()

	//加载列表数据
	getDataList()
}

const selectionChangeHandle = (selections: any[]) => {
	if (isManualSelection) {
		emit('selectChange', selections)
	}
}

const toggleRowSelection = (rowList: any[]) => {
	isManualSelection = false
	rowList.forEach((row: any) => {
		tableRef.value.toggleRowSelection(row, true)
	})
	isManualSelection = true
}

const { getDataList, sizeChangeHandle, currentChangeHandle, queryRef, resetQueryHandle, tableIndex } = useIndexQuery(state)

defineExpose({
	init,
	toggleRowSelection
})
</script>
