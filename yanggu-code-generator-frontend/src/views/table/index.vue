<template>
	<el-card class="layout-query" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="state.queryForm.projectId" style="width: 140px" clearable placeholder="请选择项目">
					<el-option v-for="item in projectList" :key="item.id" :label="item.projectName" :value="item.id"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="数据库名称" prop="databaseName">
				<el-input v-model="state.queryForm.databaseName" placeholder="请输入数据库名称" clearable></el-input>
			</el-form-item>
			<el-form-item label="表名" prop="tableName">
				<el-input v-model="state.queryForm.tableName" clearable placeholder="请输入表名"></el-input>
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
			<el-button type="primary" :icon="Upload" @click="importHandle()">导入</el-button>
			<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
			<el-button type="success" :icon="DocumentAdd" @click="generatorCodeBatch()">生成代码</el-button>
		</el-space>
		<el-table
			ref="tableRef"
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
			<el-table-column
				prop="databaseName"
				label="数据库名称"
				show-overflow-tooltip
				header-align="center"
				align="center"
				width="100"
			></el-table-column>
			<el-table-column prop="tableName" label="表名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="generatorType"
				label="生成方式"
				:formatter="(_: any, __: any, value: any) => getLabel(value, PROJECT_GENERATE_TYPES)"
				header-align="center"
				align="center"
				width="100"
			></el-table-column>
			<el-table-column prop="tableComment" label="注释" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="160">
				<template #default="scope">
					<el-row>
						<el-col :span="12">
							<el-button type="primary" link :icon="Setting" @click="editHandle(scope.row)">配置</el-button>
						</el-col>
						<el-col :span="12">
							<el-button type="primary" link :icon="Edit" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
						</el-col>
					</el-row>
					<el-row>
						<el-col :span="12">
							<el-button type="primary" link :icon="View" @click="previewHandle(scope.row)">预览</el-button>
						</el-col>
						<el-col :span="12">
							<el-dropdown>
								<el-button type="primary" link class="more-button" :icon="More">更多</el-button>
								<template #dropdown>
									<el-dropdown-menu>
										<el-dropdown-item :icon="DocumentAdd" @click="generatorCode(scope.row)">生成代码</el-dropdown-item>
										<el-dropdown-item :icon="Refresh" @click="syncHandle(scope.row)">字段同步</el-dropdown-item>
										<el-dropdown-item :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-dropdown-item>
									</el-dropdown-menu>
								</template>
							</el-dropdown>
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
		<update ref="addOrUpdateRef" @refresh-data-list="getDataList"></update>

		<!-- 导入表组件 -->
		<import ref="importRef" @refresh-data-list="getDataList"></import>

		<!-- 预览 -->
		<preview ref="previewRef" @refresh-data-list="getDataList"></preview>

		<!-- 字段配置 -->
		<field-config ref="editRef" @refresh-data-list="getDataList"></field-config>

		<!-- 模板组展示 -->
		<template-index ref="templateIndexRef" @clear-selection="clearSelectionHandler()"></template-index>
	</el-card>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { nextTick, onMounted, reactive, ref } from 'vue'
import Import from './import.vue'
import Update from './update.vue'
import Preview from '@/components/preview/index.vue'
import FieldConfig from './field-config.vue'
import TemplateIndex from './template-index.vue'
import { projectEntityListApi } from '@/api/project'
import { tableDeleteListApi, tableGenerateCheckApi, tableSyncApi, tableVOPageApi } from '@/api/table'
import { ElMessage } from 'element-plus/es'
import { ElMessageBox } from 'element-plus'
import { GeneratorProductTypeEnum, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import { useInitForm } from '@/hooks/use-init-form'
import { Delete, DocumentAdd, Edit, More, Refresh, Search, Setting, Upload, View } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenTable'
})

onMounted(() => {
	getProjectList()
})

const state: IHooksOptions = reactive({
	dataListApi: tableVOPageApi,
	deleteListApi: tableDeleteListApi,
	queryForm: {
		tableName: '',
		projectId: null,
		databaseName: ''
	}
})

const importRef = ref()
const editRef = ref()
const previewRef = ref()
const templateIndexRef = ref()
const tableRef = ref()
const projectList = ref([])

const getProjectList = () => {
	projectEntityListApi({}).then(res => {
		projectList.value = res.data
	})
}

const importHandle = () => {
	importRef.value.init()
}

const previewHandle = (tableItem: any) => {
	previewRef.value.init(tableItem.id, tableItem.projectId, tableItem.generatorType, GeneratorProductTypeEnum.TABLE)
}

const editHandle = (row: any) => {
	editRef.value.init(row)
}

//生成代码
const generatorCode = (item: any) => {
	templateIndexRef.value.init(item.tableTemplateGroupId, item.generatorType, [item.id])
}

const generatorCodeBatch = () => {
	const data = state.dataListSelections ? state.dataListSelections : []
	if (data.length === 0) {
		ElMessage.warning('请选择要生成代码的表')
		return
	}
	tableGenerateCheckApi(data).then(res => {
		const { checkResult, tableTemplateGroupId, generatorType } = res.data
		if (!checkResult) {
			ElMessage.warning('当前选择的表不是同一个项目')
		} else {
			templateIndexRef.value.init(tableTemplateGroupId, generatorType, data)
		}
	})
}

const syncHandle = (row: any) => {
	ElMessageBox.confirm(`确定同步表${row.tableName}的字段吗?`, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			tableSyncApi(row.id).then(() => {
				ElMessage.success('同步成功')
			})
		})
		.catch(() => {})
}

const clearSelectionHandler = () => {
	state.dataListSelections = []
	tableRef.value.clearSelection()
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

<style scoped>
.more-button {
	display: inline-flex;
	align-items: center;
	height: 24px; /* 强制统一高度 */
	line-height: 1; /* 统一行高 */
	padding: 0; /* 去掉内边距 */
	vertical-align: middle;
}
</style>
