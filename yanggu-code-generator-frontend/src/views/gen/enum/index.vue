<template>
	<div>
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="项目" prop="projectId">
					<el-select v-model="state.queryForm.projectId" style="width: 180px" placeholder="请选择项目" clearable filterable>
						<el-option v-for="projectItem in projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
							<option-label :label="projectItem.projectName" :desc="projectItem.projectDesc"></option-label>
						</el-option>
					</el-select>
				</el-form-item>
				<el-form-item label="枚举名称" prop="enumName">
					<el-input v-model="state.queryForm.enumName" clearable placeholder="请输入枚举名称"></el-input>
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
							<el-button type="success" :icon="DocumentAdd" @click="generatorBatchHandler()">生成</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
			<!-- 表格数据 -->
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
				<el-table-column prop="enumName" label="枚举名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="generatorType"
					label="生成类型"
					show-overflow-tooltip
					header-align="center"
					:formatter="getLabel(PROJECT_GENERATE_TYPES)"
					align="center"
				></el-table-column>
				<el-table-column prop="enumDesc" label="枚举描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemCount" label="枚举项数量" header-align="center" align="center">
					<template #default="scope">
						<el-tooltip content="配置枚举项" placement="top">
							<el-button type="primary" link @click="configEnumItemHandle(scope.row)">{{ scope.row.enumItemCount }}个</el-button>
						</el-tooltip>
					</template>
				</el-table-column>
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
						<el-row :gutter="5">
							<el-col :span="12">
								<el-button type="primary" link :icon="Setting" @click="configEnumItemHandle(scope.row)">配置</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="Edit" @click="formInitHandle(scope.row.id)">修改</el-button>
							</el-col>
						</el-row>
						<el-row :gutter="5">
							<el-col :span="12">
								<el-button type="primary" link :icon="View" @click="previewHandle(scope.row)">预览</el-button>
							</el-col>
							<el-col :span="12">
								<el-dropdown>
									<el-button type="primary" link :icon="More">更多</el-button>
									<template #dropdown>
										<el-dropdown-menu>
											<el-dropdown-item :icon="DocumentAdd" @click="generatorHandler(scope.row)">生成</el-dropdown-item>
											<el-dropdown-item :icon="Delete" @click="deleteBatchHandle(scope.row)">删除</el-dropdown-item>
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
			></el-pagination>

			<!-- 弹窗, 新增 / 修改 -->
			<enum-form ref="formRef" @refresh-data-list="getDataList()"></enum-form>

			<enum-item-index ref="enumItemIndexRef" @refresh-data-list="getDataList()"></enum-item-index>

			<preview ref="previewRef"></preview>

			<template-index ref="templateIndexRef" @clear-selection="clearSelectionHandler()"></template-index>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import EnumForm from '@/views/gen/enum/form.vue'
import EnumItemIndex from '@/views/gen/enum-item/index.vue'
import TemplateIndex from '@/views/gen/enum/template-index.vue'
import Preview from '@/business/preview/index.vue'
import { genEnumApi, genProjectApi } from '@/api'
import { useTableAction } from '@/hooks/use-table-action'
import { GenEnumEntity, GenEnumQuery, GenProjectEntity, IHooksOptions } from '@/types'
import { ElMessage } from 'element-plus'
import { getLabel } from '@/utils/enum'
import { GeneratorProductTypeEnum, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { useInitForm } from '@/hooks/use-init-form'
import { Delete, DocumentAdd, Edit, More, Plus, Refresh, Search, Setting, View } from '@element-plus/icons-vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import OptionLabel from '@/components/option/label/index.vue'

defineOptions({
	name: 'GenEnum'
})

onMounted(() => {
	getProjectList()
})

// 初始化查询表单数据
const initQueryFormData = (): GenEnumQuery => ({
	enumName: '',
	projectId: ''
})

const state = reactive({
	tableSubject: '枚举',
	deleteNameKey: 'enumName',
	dataListApi: genEnumApi.voPage,
	deleteListApi: genEnumApi.deleteList,
	initQueryFormData,
	queryForm: initQueryFormData()
} as IHooksOptions<GenEnumEntity, GenEnumQuery>)

const enumItemIndexRef = ref()
const previewRef = ref()
const templateIndexRef = ref()
const projectList = ref([] as GenProjectEntity[])

const getProjectList = () => {
	genProjectApi.entityList().then(data => {
		projectList.value = data
	})
}

const configEnumItemHandle = ({ id, enumName }: { id: string; enumName: string }) => {
	enumItemIndexRef.value.init(id, enumName)
}

const previewHandle = (row: any) => {
	previewRef.value.init(row.id, row.enumName, row.projectId, row.generatorType, GeneratorProductTypeEnum.ENUM)
}

const generatorBatchHandler = () => {
	const data = state.dataListSelections ? state.dataListSelections : []
	if (data.length === 0) {
		ElMessage.warning('请选择要生成代码的枚举')
		return
	}
	genEnumApi.generateCheck(data).then(resData => {
		const { checkResult, enumTemplateGroupId, generatorType } = resData
		if (!checkResult) {
			ElMessage.warning('当前选择的枚举不是同一个项目')
		} else {
			templateIndexRef.value.init(enumTemplateGroupId, generatorType, data)
		}
	})
}

const clearSelectionHandler = () => {
	state.dataListSelections = []
	tableRef.value.clearSelection()
}

const generatorHandler = (row: any) => {
	templateIndexRef.value.init(row.enumTemplateGroupId, row.generatorType, [row.id])
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

<style scoped>
:deep(.el-dropdown) {
	vertical-align: middle; /* 行内对齐中线 */
}
</style>
