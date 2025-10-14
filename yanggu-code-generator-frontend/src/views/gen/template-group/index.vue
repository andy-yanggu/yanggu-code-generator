<template>
	<div>
		<el-card class="layout-query" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="模板组名称" prop="groupName">
					<el-input v-model="state.queryForm.groupName" style="width: 140px" clearable placeholder="请输入模板组名称"></el-input>
				</el-form-item>
				<el-form-item label="模板组类型" prop="type">
					<el-select v-model="state.queryForm.type" style="width: 170px" clearable placeholder="请选择模板组类型">
						<el-option v-for="item in TEMPLATE_GROUP_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
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
			<el-space class="layout-space">
				<el-button type="primary" :icon="Plus" @click="addOrUpdateHandle()">新增</el-button>
				<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
				<el-upload :limit="1" :show-file-list="false" :http-request="handleManualUpload">
					<el-button type="success" :icon="Upload">导入</el-button>
				</el-upload>
				<el-button type="info" :icon="Download" @click="exportHandle()">导出</el-button>
			</el-space>
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
				<el-table-column prop="groupName" label="模板组名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="type"
					label="模板组类型"
					:formatter="getLabel(TEMPLATE_GROUP_TYPES)"
					header-align="center"
					align="center"
				></el-table-column>
				<el-table-column prop="groupDesc" label="模板组描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
								<el-button type="primary" link :icon="Setting" @click="treeData(scope.row)">配置</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="Edit" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
							</el-col>
						</el-row>
						<el-row :gutter="5">
							<el-col :span="12">
								<el-button type="primary" link :icon="CopyDocument" @click="copyTemplateGroupHandle(scope.row.id)">复制</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
							</el-col>
						</el-row>
						<el-row :gutter="5">
							<el-col :span="24">
								<el-button type="primary" link :icon="List" @click="propertyHandler(scope.row)">属性</el-button>
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
			<template-group-form ref="addOrUpdateRef" :mode="dialogMode" @refresh-data-list="getDataList"></template-group-form>
		</el-card>

		<template-tree
			ref="treeUpdateRef"
			:key="key"
			:template-group-id="currentTemplateGroup.id"
			:template-group-name="currentTemplateGroup.groupName"
			:template-group-type="currentTemplateGroup.type"
		></template-tree>

		<template-group-property ref="propertyRef"></template-group-property>
	</div>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { useInitForm } from '@/hooks/use-init-form'
import { getLabel } from '@/utils/enum'
import { nextTick, reactive, ref } from 'vue'
import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'
import TemplateGroupForm from '@/views/gen/template-group/form.vue'
import TemplateTree from '@/views/gen/template/tree.vue'
import TemplateGroupProperty from '@/views/gen/template-group-property/index.vue'
import { ElMessage } from 'element-plus'
import { genTemplateGroupApi } from '@/api/gen/template-group'
import { CopyDocument, Delete, Download, Edit, List, Plus, Refresh, Search, Setting, Upload } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenTemplateGroup'
})

const state: IHooksOptions = reactive({
	dataListApi: genTemplateGroupApi.entityPage,
	deleteListApi: genTemplateGroupApi.deleteList,
	queryForm: {
		groupName: '',
		type: ''
	},
	deleteMessage: '删除模板组，模板组下面的所有模板都会删除'
})

const tableRef = ref()
const propertyRef = ref()
const dialogMode = ref<'add' | 'update' | 'copy'>('add')
const key = ref()
const currentTemplateGroup = reactive({
	id: -1,
	groupName: '',
	type: -1
})
const treeUpdateRef = ref()

// 修改添加/更新处理函数
const addOrUpdateHandle = (id?: number) => {
	// 设置模式为添加或更新
	dialogMode.value = id ? 'update' : 'add'
	// 调用原始函数
	nextTick(() => {
		addOrUpdateRef.value.init(id)
	})
}

// 修改复制处理函数
const copyTemplateGroupHandle = (id: number) => {
	// 设置模式为复制
	dialogMode.value = 'copy'
	// 调用初始化函数
	nextTick(() => {
		addOrUpdateRef.value.init(id)
	})
}

const treeData = (row: any) => {
	key.value = Date.now()
	Object.assign(currentTemplateGroup, row)
	nextTick(() => {
		treeUpdateRef.value.init(row.id)
	})
}

const exportHandle = () => {
	const idList = state.dataListSelections ? state.dataListSelections : []
	if (idList.length === 0) {
		ElMessage.warning('请选择导出的模板组')
		return
	}
	genTemplateGroupApi.export(idList).then(() => {
		ElMessage.success('导出成功，请查看已下载的文件')
		tableRef.value.clearSelection()
		state.dataListSelections = []
	})
}

// 自定义上传处理
const handleManualUpload = (options: any) => {
	const { file } = options
	const formData = new FormData()
	formData.append('file', file)

	genTemplateGroupApi
		.import(formData)
		.then(() => {
			ElMessage.success('模板组导入成功')
		})
		.then(() => {
			getDataList()
		})
}

const propertyHandler = (row: any) => {
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
	resetQueryHandle,
	tableIndex
} = useIndexQuery(state)

const { addOrUpdateRef } = useInitForm()
</script>
<style scoped></style>
