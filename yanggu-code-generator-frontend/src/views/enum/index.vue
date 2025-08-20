<template>
	<div>
		<el-card class="layout-query" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="项目" prop="projectId">
					<el-select v-model="state.queryForm.projectId" style="width: 140px" clearable placeholder="请选择项目">
						<el-option v-for="item in projectList" :key="item.id" :value="item.id" :label="item.projectName">{{ item.projectName }}</el-option>
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

		<el-card shadow="hover">
			<el-space :size="'large'" class="layout-space">
				<el-button type="primary" :icon="Plus" @click="addOrUpdateHandle()">新增</el-button>
				<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
				<el-button type="success" :icon="DocumentAdd" @click="generatorBatchHandler()">生成代码</el-button>
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
								<el-button type="primary" link :icon="Setting" @click="configEnumItemHandle(scope.row.id)">配置</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="Edit" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
							</el-col>
						</el-row>
						<el-row :gutter="5">
							<el-col :span="12">
								<el-button type="primary" link :icon="View" @click="previewHandle(scope.row)">预览</el-button>
							</el-col>
							<el-col :span="12">
								<el-dropdown>
									<el-button type="primary" link class="more-button" :icon="More">更多</el-button>
									<template #dropdown>
										<el-dropdown-menu>
											<el-dropdown-item :icon="DocumentAdd" @click="generatorHandler(scope.row)">生成代码</el-dropdown-item>
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
			<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>

			<enum-item-index ref="enumItemIndexRef"></enum-item-index>

			<preview ref="previewRef" :key="previewKey"></preview>

			<template-index ref="templateIndexRef" @clear-selection="clearSelectionHandler()"></template-index>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { nextTick, onMounted, reactive, ref } from 'vue'
import AddOrUpdate from '@/views/enum/add-or-update.vue'
import EnumItemIndex from '@/views/enum-item/index.vue'
import TemplateIndex from '@/views/enum/template-index.vue'
import Preview from '@/components/preview/index.vue'
import { projectEntityListApi } from '@/api/project'
import { ElMessage } from 'element-plus'
import { enumDeleteListApi, enumGenerateCheckApi, enumVOPageApi } from '@/api/enum'
import { getLabel } from '@/utils/enum'
import { GeneratorProductTypeEnum, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { useInitForm } from '@/hooks/use-init-form'
import { Delete, DocumentAdd, Edit, More, Plus, Refresh, Search, Setting, View } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenEnum'
})

onMounted(() => {
	getProjectList()
})

const state: IHooksOptions = reactive({
	dataListApi: enumVOPageApi,
	deleteListApi: enumDeleteListApi,
	queryForm: {
		enumName: '',
		projectId: ''
	}
})

const enumItemIndexRef = ref()
const previewRef = ref()
const tableRef = ref()
const templateIndexRef = ref()
const projectList = ref([])
const previewKey = ref('')

const getProjectList = () => {
	projectEntityListApi({}).then((res: any) => {
		projectList.value = res.data
	})
}

const configEnumItemHandle = (id: number) => {
	enumItemIndexRef.value.init(id)
}

const previewHandle = (row: any) => {
	previewKey.value = `${Date.now()}`
	nextTick(() => {
		previewRef.value.init(row.id, row.projectId, row.generatorType, GeneratorProductTypeEnum.ENUM)
	})
}

const generatorBatchHandler = () => {
	const data = state.dataListSelections ? state.dataListSelections : []
	if (data.length === 0) {
		ElMessage.warning('请选择要生成代码的枚举')
		return
	}
	enumGenerateCheckApi(data).then(res => {
		const { checkResult, enumTemplateGroupId, generatorType } = res.data
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
