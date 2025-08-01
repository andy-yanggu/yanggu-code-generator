<template>
	<el-dialog v-model="dialogVisible" title="模板配置" width="80%" @close="dialogVisible = false">
		<el-card class="layout-query" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="模板名称" prop="templateName">
					<el-input v-model="state.queryForm.templateName" clearable placeholder="请输入模板名称"></el-input>
				</el-form-item>
				<el-form-item label="模板类型" prop="templateType">
					<el-select v-model="state.queryForm.templateType" style="width: 160px" clearable placeholder="请选择模板类型">
						<el-option v-for="item in TEMPLATE_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card shadow="hover">
			<el-space :size="'large'" class="layout-space">
				<el-button type="primary" :icon="Plus" @click="addOrUpdateHandle()">新增</el-button>
				<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
			</el-space>
			<el-table
				v-loading="state.dataListLoading"
				:data="state.dataList"
				border
				class="layout-table"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="templateName" label="模板名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="generatorPath" label="模板路径" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="templateType"
					label="模板类型"
					header-align="center"
					align="center"
					:formatter="(_: any, __: any, value: any) => getLabel(value, TEMPLATE_TYPES)"
				></el-table-column>
				<el-table-column prop="templateDesc" label="模板描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="createTime"
					label="创建时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"
				></el-table-column>
				<el-table-column
					prop="updateTime"
					label="修改时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"
				></el-table-column>
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
					<template #default="scope">
						<el-button type="primary" link :icon="Edit" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
						<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
					</template>
				</el-table-column>
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
			<add-or-update ref="addOrUpdateRef" :template-group-id="currentGroupId" @refresh-data-list="getDataList"></add-or-update>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { reactive, ref } from 'vue'
import AddOrUpdate from './add-or-update.vue'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import { templateDeleteListApi, templateEntityPageApi } from '@/api/template'
import { useInitForm } from '@/hooks/use-init-form'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'

const state: IHooksOptions = reactive({
	createdIsNeed: false,
	dataListApi: templateEntityPageApi,
	deleteListApi: templateDeleteListApi,
	queryForm: {
		templateGroupId: null,
		templateName: '',
		templateType: null
	}
})

const dialogVisible = ref(false)
const currentGroupId = ref<number>(-1)

const init = (row: any) => {
	dialogVisible.value = true
	resetQueryHandle()
	state.queryForm.templateGroupId = row.id
	getDataList()
	currentGroupId.value = row.id
}

const {
	getDataList,
	selectionChangeHandle,
	sizeChangeHandle,
	currentChangeHandle,
	deleteBatchHandle,
	sortChangeHandle,
	queryRef,
	resetQueryHandle,
	tableIndex
} = useIndexQuery(state)

const { addOrUpdateRef, addOrUpdateHandle } = useInitForm()

defineExpose({
	init
})
</script>
