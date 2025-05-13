<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
					<el-form-item prop="groupName">
			  <el-input v-model="state.queryForm.groupName" clearable placeholder="请输入模板组名称"></el-input>
			</el-form-item>
			<el-form-item prop="type">
            <el-form-item prop="type">
                <el-select v-model="state.queryForm.type" clearable placeholder="请选择模板组类型（0-项目，1-表）">
                    <el-option v-for="item in gen_template_group_type_enum" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
            </el-form-item>
			</el-form-item>
			<el-form-item>
				<el-button icon="Search" type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button icon="RefreshRight" @click="resetQueryRef()">重置</el-button>
			</el-form-item>
            <el-form-item>
                <el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
            </el-form-item>
            <el-form-item>
                <el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
            </el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="groupName" label="模板组名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
        <el-table-column prop="type" label="模板组类型（0-项目，1-表）" show-overflow-tooltip header-align="center" align="center" :formatter="handlertype"></el-table-column>
			<el-table-column prop="groupDesc" label="模板组描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="deleteBatchHandle(scope.row.id)">删除</el-button>
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
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import { gen_template_group_type_enum } from '@/constant/enum'

const state: IHooksOptions = reactive({
    dataListUrl: '/templateGroup/entityPage',
    deleteUrl: '/templateGroup/deleteList',
    queryForm: {
        groupName: '', 
        type: ''
    }
})

const queryRef = ref()
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(id)
}
const resetQueryRef = () => {
  queryRef.value.resetFields()
}

const handlertype = (row: any) => {
    return gen_template_group_type_enum.find(item => item.value === row.type)?.label
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>
