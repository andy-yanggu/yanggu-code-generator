<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item>
				<el-button icon="Search" type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button icon="RefreshRight" @click="reset(queryRef)">重置</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-space>
			<el-space>
				<el-button icon="Plus" type="primary" @click="addOrUpdateHandle()">新增</el-button>
			</el-space>
			<el-space>
				<el-button icon="Delete" plain type="danger" @click="deleteBatchHandle()">批量删除</el-button>
			</el-space>
		</el-space>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column prop="id" label="id" header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableId" label="表ID" header-align="center" align="center"></el-table-column>
			<el-table-column prop="fieldName" label="字段名称" header-align="center" align="center"></el-table-column>
			<el-table-column prop="fieldType" label="字段类型" header-align="center" align="center"></el-table-column>
			<el-table-column prop="fieldComment" label="字段说明" header-align="center" align="center"></el-table-column>
			<el-table-column prop="attrName" label="属性名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="attrType" label="属性类型" header-align="center" align="center"></el-table-column>
			<el-table-column prop="packageName" label="属性包名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="sort" label="排序" header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="autoFill"
				label="自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE"
				header-align="center"
				align="center"
			></el-table-column>
			<el-table-column prop="primaryPk" label="主键 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="logicDelete" label="逻辑删除 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="logicDeleteValue" label="逻辑删除值" header-align="center" align="center"></el-table-column>
			<el-table-column prop="logicNotDeleteValue" label="逻辑未删除值" header-align="center" align="center"></el-table-column>
			<el-table-column prop="baseField" label="基类字段 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="formItem" label="表单项 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="formRequired" label="表单必填 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="formType" label="表单类型" header-align="center" align="center"></el-table-column>
			<el-table-column prop="formDict" label="表单字典类型" header-align="center" align="center"></el-table-column>
			<el-table-column prop="formValidator" label="表单效验" header-align="center" align="center"></el-table-column>
			<el-table-column prop="gridItem" label="列表项 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="gridSort" label="列表排序 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="queryItem" label="查询项 0：否  1：是" header-align="center" align="center"></el-table-column>
			<el-table-column prop="queryType" label="查询方式" header-align="center" align="center"></el-table-column>
			<el-table-column prop="queryFormType" label="查询表单类型" header-align="center" align="center"></el-table-column>
			<el-table-column prop="createTime" label="创建时间" header-align="center" align="center"></el-table-column>
			<el-table-column prop="updateTime" label="修改时间" header-align="center" align="center"></el-table-column>
			<el-table-column prop="isDelete" label="是否删除（0未删除, 1删除）" header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="deleteBatchHandle(scope.row.id)">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			:current-page="state.page"
			:page-sizes="state.pageSizes"
			:page-size="state.limit"
			:total="state.total"
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
		>
		</el-pagination>

		<!-- 弹窗, 新增 / 修改 -->
		<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'

const state: IHooksOptions = reactive({
	dataListUrl: '/tableField/entityPage',
	deleteUrl: '/tableField/deleteList',
	queryForm: {}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(id)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle, reset } = useCrud(state)
</script>
