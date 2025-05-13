<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
		<#list queryList as field>
			<el-form-item prop="${field.attrName}">
			<#if field.queryFormType == 'text' || field.queryFormType == 'textarea' || field.queryFormType == 'editor'>
			  <el-input v-model="state.queryForm.${field.attrName}" clearable placeholder="请输入${field.fieldComment!}"></el-input>
			<#elseif field.queryFormType == 'select'>
            <el-form-item prop="${field.attrName}">
                <el-select v-model="state.queryForm.${field.attrName}" clearable placeholder="请选择${field.fieldComment!}">
                    <el-option v-for="item in ${tableName}_${field.attrName}_enum" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
            </el-form-item>
			<#elseif field.queryFormType == 'radio'>
			  <#if field.formDict??>
			  <ma-dict-radio v-model="state.queryForm.${field.attrName}" dict-type="${field.formDict}"></ma-dict-radio>
			  <#else>
			  <el-radio-group v-model="state.queryForm.${field.attrName}">
				<el-radio :label="0">单选</el-radio>
			  </el-radio-group>
			  </#if>
			<#elseif field.queryFormType == 'date'>
                <el-date-picker
                        v-model="queryFormModel.${field.attrName}"
                        type="daterange"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        clearable
                >
                </el-date-picker>
            <#elseif field.queryFormType == 'datetime'>
                <el-date-picker
                        v-model="queryFormModel.${field.attrName}"
                        type="datetimerange"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        clearable
                >
                </el-date-picker>
			<#else>
			  <el-input v-model="state.queryForm.${field.attrName}" placeholder="${field.fieldComment!}"></el-input>
			</#if>
			</el-form-item>
		  </#list>
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
	    <#list gridList as field>
		  <#if field.formDict??>
        <el-table-column prop="${field.attrName}" label="${field.fieldComment!}" show-overflow-tooltip header-align="center" align="center" :formatter="handler${field.attrName}"></el-table-column>
		  <#else>
			<el-table-column prop="${field.attrName}" label="${field.fieldComment!}" show-overflow-tooltip header-align="center" align="center"></el-table-column>
		  </#if>
        </#list>
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
<#list formList as field>
<#if field.formType == 'select'>
import { ${tableName}_${field.attrName}_enum } from '@/constant/enum'
</#if>
</#list>

const state: IHooksOptions = reactive({
    dataListUrl: '/${functionName}/entityPage',
    deleteUrl: '/${functionName}/deleteList',
    queryForm: {
        <#list queryList as field>
        <#if field.formType == 'date'>
        startDate: '',
        endDate: ''<#sep>, </#sep>
        <#elseif field.formType == 'datetime'>
        startDateTime: '',
        endDateTime: ''<#sep>, </#sep>
        <#else>
        ${field.attrName}: ''<#sep>, </#sep>
        </#if>
        </#list>
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

<#list gridList as field>
<#if field.formDict??>
const handler${field.attrName} = (row: any) => {
    return ${tableName}_${field.attrName}_enum.find(item => item.value === row.${field.attrName})?.label
}
</#if>
</#list>

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>
