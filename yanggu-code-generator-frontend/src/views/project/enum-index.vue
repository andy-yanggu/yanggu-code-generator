<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item prop="enumName">
				<el-input v-model="state.queryForm.enumName" clearable placeholder="请输入枚举名称"></el-input>
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
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="enumName" label="枚举名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="enumDesc" label="枚举描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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

		<!-- 弹窗, 新增 / 修改 -->
		<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>

		<enum-item-index ref="enumItemIndexRef"></enum-item-index>

		<preview ref="previewRef"></preview>

		<template-index ref="templateIndexRef" :key="currentTemplateGroupIdTs"></template-index>
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { nextTick, reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import EnumItemIndex from '@/views/enum-item/index.vue'
import TemplateIndex from '@/views/enum/template-index.vue'
import Preview from './preview.vue'
import { projectEntityListApi } from '@/api/project'
import { ElMessage } from 'element-plus'
import { enumGenerateCheckApi } from '@/api/enum'

const state: IHooksOptions = reactive({
	dataListUrl: '/enum/entityPage',
	queryForm: {
		enumName: '',
		projectId: ''
	}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const enumItemIndexRef = ref()
const previewRef = ref()
const templateIndexRef = ref()
const currentTemplateGroupIdTs = ref()
const resetQueryRef = () => {
	queryRef.value.resetFields()
}
const init = (projectId: number) => {
	state.queryForm.projectId = projectId
	getDataList()
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle } = useCrud(state)

defineExpose({
	init
})
</script>
