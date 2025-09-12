<template>
	<div>
		<el-card class="layout-query" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="基类包名" prop="packageName">
					<el-input v-model="state.queryForm.packageName" clearable placeholder="请输入基类包名"></el-input>
				</el-form-item>
				<el-form-item label="基类类名" prop="className">
					<el-input v-model="state.queryForm.className" clearable placeholder="请输入基类类名"></el-input>
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
			</el-space>
			<el-table
				v-loading="state.dataListLoading"
				:data="state.dataList"
				border
				max-height="60vh"
				class="layout-table"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="baseClassName" label="基类名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="packageName" label="基类包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="className" label="基类类名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="fields" label="基类字段" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
						<el-button type="primary" link :icon="CopyDocument" @click="copyBaseClassHandle(scope.row.id)">复制</el-button>
						<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
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
			<base-class-form ref="addOrUpdateRef" :mode="dialogMode" @refresh-data-list="getDataList"></base-class-form>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { useInitForm } from '@/hooks/use-init-form'
import { nextTick, reactive, ref } from 'vue'
import BaseClassForm from '@/views/gen/base-class/form.vue'
import { baseClassDeleteListApi, baseClassEntityPageApi } from '@/api/gen/base-class'
import { CopyDocument, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenBaseClass'
})

const state: IHooksOptions = reactive({
	dataListApi: baseClassEntityPageApi,
	deleteListApi: baseClassDeleteListApi,
	queryForm: {
		packageName: '',
		className: ''
	}
})

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

// 添加一个响应式变量来控制对话框模式
const dialogMode = ref<'add' | 'update' | 'copy'>('add')

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
const copyBaseClassHandle = (id: number) => {
	// 设置模式为复制
	dialogMode.value = 'copy'
	// 调用初始化函数
	nextTick(() => {
		addOrUpdateRef.value.init(id)
	})
}

const { addOrUpdateRef } = useInitForm()
</script>
