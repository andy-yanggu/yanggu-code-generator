<template>
	<div>
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="项目名称" prop="projectName">
					<el-input v-model="state.queryForm.projectName" clearable placeholder="请输入项目名称" style="width: 140px"></el-input>
				</el-form-item>
				<el-form-item label="生成方式" prop="generatorType">
					<el-select
						v-model="state.queryForm.generatorType"
						:options="PROJECT_GENERATE_TYPES"
						style="width: 150px"
						filterable
						clearable
						placeholder="请选择生成方式"
					></el-select>
				</el-form-item>
				<el-form-item label="创建时间" prop="dateTimeRange" style="width: 340px">
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
					<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card ref="tableCardRef" class="layout-table-card" shadow="hover">
			<!-- 表格工具栏 -->
			<template #header>
				<table-tool-bar
					v-if="tableCardRef"
					v-model:show-search="queryShow"
					v-model:query-loading="state.dataListLoading"
					:table-card-ref="tableCardRef"
					@get-data-list="getDataList()"
				>
					<template #left>
						<el-space size="default">
							<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
							<el-button type="danger" :loading="state.deleteLoading" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
			<el-table
				ref="tableRef"
				v-loading="state.dataListLoading"
				:data="state.dataList"
				border
				max-height="60vh"
				class="layout-table"
				header-cell-class-name="layout-table-header"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="projectName" label="项目名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="projectPort" label="项目端口" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="author" label="作者" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="projectDesc" label="项目描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="generatorType"
					label="生成方式"
					:formatter="getLabel(PROJECT_GENERATE_TYPES)"
					header-align="center"
					align="center"
				></el-table-column>
				<el-table-column prop="projectPackage" label="项目包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="createTime"
					label="创建时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"
				></el-table-column>
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="160">
					<template #default="scope">
						<el-row>
							<el-col :span="12">
								<el-button type="primary" link :icon="Edit" @click="formInitHandle(scope.row.id)">修改</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="DocumentAdd" @click="generatorCode(scope.row)">生成</el-button>
							</el-col>
						</el-row>
						<el-row>
							<el-col :span="12">
								<el-button type="primary" link :icon="View" @click="previewHandle(scope.row)">预览</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
							</el-col>
						</el-row>
					</template>
				</el-table-column>
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

			<!-- 弹窗, 新增 / 修改 -->
			<project-form ref="formRef" @refresh-data-list="getDataList"></project-form>

			<!-- 预览 -->
			<preview ref="previewRef"></preview>

			<!-- 生成代码步骤条 -->
			<steps ref="stepsRef"></steps>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import { useTableAction } from '@/hooks'
import { reactive, ref } from 'vue'
import Preview from '@/business/preview/index.vue'
import { genProjectApi } from '@/api'
import { GenProjectEntity, GenProjectQuery, IHooksOptions } from '@/types'
import Steps from '@/views/gen/project/steps.vue'
import ProjectForm from '@/views/gen/project/form.vue'
import { GeneratorProductTypeEnum, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { useInitForm } from '@/hooks/use-init-form'
import { getLabel } from '@/utils/enum'
import { Delete, DocumentAdd, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'

defineOptions({
	name: 'GenProject'
})

const state = reactive({
	dataListApi: genProjectApi.entityPage,
	deleteListApi: genProjectApi.deleteList,
	queryForm: {
		projectName: '',
		generatorType: '',
		dateTimeRange: [] as string[]
	},
	deleteMessage: '删除项目会删除项目下的所有表，是否继续？'
} as IHooksOptions<GenProjectEntity, GenProjectQuery>)

const previewRef = ref()
const stepsRef = ref()

const previewHandle = (projectItem: GenProjectEntity) => {
	previewRef.value.init(projectItem.id, projectItem.projectName, projectItem.id, projectItem.generatorType, GeneratorProductTypeEnum.PROJECT)
}

const generatorCode = (item: GenProjectEntity) => {
	stepsRef.value.init(item)
}

const {
	getDataList,
	selectionChangeHandle,
	sizeChangeHandle,
	currentChangeHandle,
	deleteBatchHandle,
	sortChangeHandle,
	queryRef,
	queryShow,
	tableCardRef,
	tableRef,
	resetQueryHandle,
	tableIndex
} = useTableAction(state)

const { formRef, formInitHandle } = useInitForm()
</script>
