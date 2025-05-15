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
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import { TEMPLATE_TYPES } from '@/constant/enum'

const emit = defineEmits(['selectChange'])
const tableRef = ref()
const state: IHooksOptions = reactive({
	dataListUrl: '/template/voPage',
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

const selectionChangeHandle = (selections: any[]) => {
	emit('selectChange', selections)
}

const { getDataList, sizeChangeHandle, currentChangeHandle } = useCrud(state)

defineExpose({
	init,
	toggleRowSelection: (row, selected) => {
		tableRef.value.toggleRowSelection(row, selected)
	}
})
</script>
