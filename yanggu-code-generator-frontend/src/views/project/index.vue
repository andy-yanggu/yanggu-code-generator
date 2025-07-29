<template>
	<el-card class="layout-query" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="项目名称" prop="projectName">
				<el-input v-model="state.queryForm.projectName" clearable placeholder="请输入项目名称"></el-input>
			</el-form-item>
			<el-form-item label="创建时间" prop="dateTimeRange" style="width: 350px">
				<el-date-picker
					v-model="state.queryForm.dateTimeRange"
					value-format="YYYY-MM-DD HH:mm:ss"
					type="datetimerange"
					range-separator="-"
					start-placeholder="开始时间"
					end-placeholder="结束时间"
					:default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
				></el-date-picker>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" :icon="Search" @click="getDataList()">查询</el-button>
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
			<el-table-column prop="projectName" label="项目名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="projectPackage" label="项目包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="generatorType"
				label="生成方式"
				:formatter="(_: any, __: any, value: any) => getLabel(value, PROJECT_GENERATE_TYPES)"
				header-align="center"
				align="center"
			></el-table-column>
			<el-table-column prop="projectDesc" label="项目描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="createTime"
				label="创建时间"
				show-overflow-tooltip
				width="120"
				header-align="center"
				align="center"
				sortable="custom"
			></el-table-column>
			<el-table-column
				prop="updateTime"
				label="修改时间"
				show-overflow-tooltip
				width="120"
				header-align="center"
				align="center"
				sortable="custom"
			></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link :icon="Edit" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link :icon="DocumentAdd" @click="generatorCode(scope.row)">生成代码</el-button>
					<el-button type="primary" link :icon="View" @click="previewHandle(scope.row)">预览</el-button>
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
		<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>

		<!-- 预览 -->
		<preview ref="previewRef" @refresh-data-list="getDataList"></preview>

		<steps ref="stepsRef" :key="currentProjectIdTs"></steps>
	</el-card>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { nextTick, reactive, ref } from 'vue'
import AddOrUpdate from '@/views/project/add-or-update.vue'
import Preview from '@/views/project/preview.vue'
import { projectDeleteListApi, projectEntityPageApi } from '@/api/project'
import Steps from '@/views/project/steps.vue'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { useInitForm } from '@/hooks/use-init-form'
import { getLabel } from '@/utils/enum'
import { Delete, DocumentAdd, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenProject'
})

const state: IHooksOptions = reactive({
	dataListApi: projectEntityPageApi,
	deleteListApi: projectDeleteListApi,
	queryForm: {
		projectName: '',
		dateTimeRange: []
	},
	deleteMessage: '删除项目会删除项目下的所有表，是否继续?'
})

const previewRef = ref()
const currentProjectIdTs = ref()
const stepsRef = ref()

const previewHandle = (projectItem: any) => {
	previewRef.value.init(projectItem)
}

const generatorCode = item => {
	currentProjectIdTs.value = Date.now()
	nextTick(() => {
		stepsRef.value.init(item)
	})
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
</script>
