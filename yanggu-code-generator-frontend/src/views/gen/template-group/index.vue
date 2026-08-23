<template>
	<div>
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="模板组名称" prop="groupName">
					<el-input v-model="state.queryForm.groupName" style="width: 140px" clearable placeholder="请输入模板组名称"></el-input>
				</el-form-item>
				<el-form-item label="模板组类型" prop="type">
					<el-select
						v-model="state.queryForm.type"
						:options="TEMPLATE_GROUP_TYPES"
						style="width: 170px"
						placeholder="请选择模板组类型"
						filterable
						clearable></el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card ref="tableCardRef" class="layout-table-card" :class="{ 'is-maximized': tableCardMaximized }" shadow="hover">
			<!-- 表格工具栏 -->
			<template #header>
				<table-tool-bar
					v-model:show-search="queryShow"
					v-model:query-loading="state.dataListLoading"
					v-model:maximized="tableCardMaximized"
					@get-data-list="getDataList()">
					<template #left>
						<el-space size="default">
							<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
							<el-button type="danger" :loading="state.deleteLoading" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
							<el-upload :limit="1" :show-file-list="false" :http-request="({ file }) => importHandle(file)">
								<el-button type="success" :icon="Upload">导入</el-button>
							</el-upload>
							<el-button type="info" :icon="Download" @click="exportHandle()">导出</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
			<el-table
				ref="tableRef"
				v-loading="state.dataListLoading!"
				:data="state.dataList"
				border
				max-height="60vh"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle">
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="groupName" label="模板组名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="type"
					label="模板组类型"
					:formatter="getLabel(TEMPLATE_GROUP_TYPES)"
					header-align="center"
					align="center"></el-table-column>
				<el-table-column prop="groupDesc" label="模板组描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="groupPropertyCount" label="属性数量" show-overflow-tooltip header-align="center" align="center">
					<template #default="scope">
						<el-tooltip content="配置属性" placement="top">
							<el-button type="primary" link @click="propertyHandler(scope.row)">{{ scope.row.groupPropertyCount }}个</el-button>
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
					sortable="custom"></el-table-column>
				<el-table-column
					prop="updateTime"
					label="修改时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"></el-table-column>
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
					<template #default="scope">
						<el-row :gutter="5">
							<el-col :span="12">
								<el-button type="primary" link :icon="Files" @click="treeData(scope.row)">模板</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="List" @click="propertyHandler(scope.row)">属性</el-button>
							</el-col>
						</el-row>
						<el-row :gutter="5">
							<el-col :span="12">
								<el-button type="primary" link :icon="Edit" @click="formInitHandle(scope.row.id)">修改</el-button>
							</el-col>
							<el-col :span="12">
								<el-dropdown>
									<el-button type="primary" link :icon="More">更多</el-button>
									<template #dropdown>
										<el-dropdown-menu>
											<el-dropdown-item :icon="CopyDocument" @click="formInitHandle({ type: 'copy', id: scope.row.id })">复制</el-dropdown-item>
											<el-dropdown-item :icon="Download" @click="exportHandle(scope.row.id)">导出</el-dropdown-item>
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
				@current-change="currentChangeHandle"></el-pagination>

			<!-- 弹窗, 新增 / 修改 -->
			<template-group-form ref="formRef" @refresh-data-list="getDataList"></template-group-form>
		</el-card>

		<template-tree
			ref="treeUpdateRef"
			:template-group-id="currentTemplateGroup.id"
			:template-group-name="currentTemplateGroup.groupName"
			:template-group-type="currentTemplateGroup.type"></template-tree>

		<template-group-property
			ref="propertyRef"
			:template-group-id="currentTemplateGroup.id"
			:template-group-name="currentTemplateGroup.groupName"
			@refresh-data-list="getDataList"></template-group-property>
	</div>
</template>

<script setup lang="ts">
import { getLabel } from '@/utils/enum'
import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'
import TemplateGroupForm from '@/views/gen/template-group/form.vue'
import TemplateTree from '@/views/gen/template/tree.vue'
import TemplateGroupProperty from '@/views/gen/template-group-property/index.vue'
import { CopyDocument, Delete, Download, Edit, Files, List, More, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue'
import { useInitForm, useTableAction } from '@/hooks'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import { genTemplateGroupApi } from '@/api'
import { GenTemplateGroupEntity, GenTemplateGroupQuery, IHooksOptions } from '@/types'

defineOptions({
	name: 'GenTemplateGroup'
})

// 初始化表单查询参数
const initQueryFormData = (): GenTemplateGroupQuery => ({
	groupName: '',
	type: ''
})

const state = reactive({
	tableSubject: '模板组',
	deleteNameKey: 'groupName',
	dataListApi: genTemplateGroupApi.voPage,
	deleteListApi: genTemplateGroupApi.deleteList,
	exportApi: genTemplateGroupApi.export,
	importApi: genTemplateGroupApi.import,
	initQueryFormData,
	queryForm: initQueryFormData(),
	deleteMessage: '删除模板组，模板组下面的所有模板、属性都会删除',
	importSuccessMessage: '模板组导入成功'
} as IHooksOptions<GenTemplateGroupEntity, GenTemplateGroupQuery>)

const propertyRef = ref()
const currentTemplateGroup = reactive({
	id: -1,
	groupName: '',
	type: -1
})
const treeUpdateRef = ref()

const treeData = (row: any) => {
	Object.assign(currentTemplateGroup, row)
	nextTick(() => {
		treeUpdateRef.value.init(row.id)
	})
}

const propertyHandler = (row: any) => {
	Object.assign(currentTemplateGroup, row)
	nextTick(() => {
		propertyRef.value.init(row.id)
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
	queryShow,
	tableCardRef,
	tableCardMaximized,
	tableRef,
	resetQueryHandle,
	tableIndex,
	exportHandle,
	importHandle
} = useTableAction(state)

const { formRef, formInitHandle } = useInitForm()
</script>
<style scoped>
:deep(.el-dropdown) {
	vertical-align: middle; /* 行内对齐中线 */
}
</style>
