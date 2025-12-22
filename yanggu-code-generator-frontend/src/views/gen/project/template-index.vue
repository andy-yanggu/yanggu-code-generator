<template>
	<el-card class="layout-query-card" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" label-position="right" label-width="auto" @keyup.enter="getDataList()">
			<form-search-grid>
				<!-- 第一行 -->
				<template #item-0>
					<el-form-item label="模板组名称" prop="templateGroupName">
						<el-input v-model="state.queryForm.templateGroupName" style="width: 140px" clearable placeholder="请输入模板组名称"></el-input>
					</el-form-item>
				</template>
				<template #item-1>
					<el-form-item label="模板组类型" prop="templateGroupType">
						<el-select
							v-model="state.queryForm.templateGroupType"
							:options="TEMPLATE_GROUP_TYPES"
							style="width: 160px"
							filterable
							clearable
							placeholder="请选择模板组类型"
						></el-select>
					</el-form-item>
				</template>
				<template #item-2>
					<el-form-item label="目录/文件名称" prop="fileName">
						<el-input v-model="state.queryForm.fileName" style="width: 160px" clearable placeholder="请输入目录/文件名称"></el-input>
					</el-form-item>
				</template>

				<!-- 第二行 -->
				<template #item-3>
					<el-form-item label="模板类型" prop="templateType">
						<el-select
							v-model="state.queryForm.templateType"
							:options="TEMPLATE_TYPES"
							style="width: 150px"
							filterable
							clearable
							placeholder="请选择模板类型"
						></el-select>
					</el-form-item>
				</template>

				<!-- 自定义操作区 -->
				<template #actions>
					<el-form-item>
						<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
					</el-form-item>
					<el-form-item>
						<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
					</el-form-item>
				</template>
			</form-search-grid>
		</el-form>
	</el-card>
	<el-card shadow="hover">
		<el-table
			ref="tableRef"
			v-loading="state.dataListLoading"
			row-key="id"
			:data="state.dataList"
			border
			max-height="60vh"
			class="layout-table"
			header-cell-class-name="layout-table-header"
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
				:formatter="getLabel(TEMPLATE_GROUP_TYPES)"
			></el-table-column>
			<el-table-column prop="fileName" label="目录/文件名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="templateType"
				label="模板类型"
				header-align="center"
				align="center"
				:formatter="getLabel(TEMPLATE_TYPES)"
			></el-table-column>
			<el-table-column prop="generatorPath" label="模板路径" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="templateDesc" label="描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
		</el-table>
		<el-pagination
			:current-page="state.pageNum"
			:page-sizes="state.pageSizes"
			:page-size="state.pageSize"
			:total="state.total"
			background
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
		>
		</el-pagination>
	</el-card>
</template>

<script setup lang="ts">
import { useTableAction } from '@/hooks'
import { reactive } from 'vue'
import { TEMPLATE_GROUP_TYPES, TEMPLATE_TYPES } from '@/constant/enum'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getLabel } from '@/utils/enum'
import { genTemplateApi } from '@/api'
import { GenTemplateEntity, GenTemplateQuery, IHooksOptions } from '@/types'
import FormSearchGrid from '@/components/form/search-grid/index.vue'

defineOptions({
	name: 'GenProjectTemplate'
})

const emit = defineEmits(['selectChange'])
const state = reactive({
	dataListApi: genTemplateApi.voPage,
	mountedGetData: false,
	queryForm: {
		templateGroupIdList: [],
		templateGroupName: '',
		fileName: '',
		templateGroupType: '',
		templateType: ''
	}
} as IHooksOptions<GenTemplateEntity, GenTemplateQuery>)
let isManualSelection = true

const init = (templateGroupIdList: number[]) => {
	state.queryForm.templateGroupIdList = templateGroupIdList

	//重置查询表单并且查询数据
	resetQueryHandle()
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

const { getDataList, sizeChangeHandle, currentChangeHandle, queryRef, tableRef, resetQueryHandle, tableIndex } = useTableAction(state)

defineExpose({
	init,
	toggleRowSelection
})
</script>
