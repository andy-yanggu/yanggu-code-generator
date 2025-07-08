<template>
	<el-card class="layout-query" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
		<#list queryList as field>
			<#if field.queryFormType == 'date'>
			<el-form-item label="${field.fieldComment}" prop="dateRange" style="width: 350px">
			<#elseif field.queryFormType == 'datetime'>
			<el-form-item label="${field.fieldComment}" prop="dateTimeRange" style="width: 350px">
			<#else>
			<el-form-item label="${field.fieldComment}" prop="${field.attrName}">
			</#if>
			<#if field.queryFormType == 'text' || field.queryFormType == 'textarea' || field.queryFormType == 'editor'>
                <el-input v-model="state.queryForm.${field.attrName}" clearable placeholder="请输入${field.fieldComment}"></el-input>
			<#elseif field.queryFormType == 'select'>
                <el-select v-model="state.queryForm.${field.attrName}" clearable placeholder="请选择${field.fieldComment}">
                    <el-option v-for="item in ${enumNameAllUpper}_ENUM" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
			<#elseif field.queryFormType == 'radio'>
                <el-radio-group v-model="state.queryForm.${field.attrName}">
                    <el-radio v-for="item in ${enumNameAllUpper}_ENUM" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                </el-radio-group>
			<#elseif field.queryFormType == 'checkbox'>
                <el-checkbox-group v-model="state.queryForm.${field.attrName}">
                    <el-checkbox v-for="item in ${enumNameAllUpper}_ENUM" :key="item.value" :label="item.label" :value="item.value">{{ item.label }}</el-checkbox>
                </el-checkbox-group>
			<#elseif field.queryFormType == 'date'>
                <el-date-picker
                        v-model="state.queryForm.dateRange"
                        type="daterange"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        clearable
						start-placeholder="开始日期"
						end-placeholder="结束日期"
						:default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
                >
                </el-date-picker>
            <#elseif field.queryFormType == 'datetime'>
                <el-date-picker
                        v-model="state.queryForm.dateTimeRange"
                        type="datetimerange"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        clearable
						start-placeholder="开始时间"
						end-placeholder="结束时间"
						:default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
                >
                </el-date-picker>
			<#else>
                <el-input v-model="state.queryForm.${field.attrName}" placeholder="请输入${field.fieldComment}"></el-input>
			</#if>
			</el-form-item>
        </#list>
			<el-form-item>
				<el-button type="primary" :icon="Search" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button @click="resetQueryRef()">重置</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card shadow="hover">
		<el-space :size="'large'">
			<el-button type="primary" :icon="Plus" @click="addOrUpdateHandle()">新增</el-button>
			<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
		</el-space>
		<el-table
			v-loading="state.dataListLoading"
			:data="state.dataList"
			border
			class="layout-table"
			@selection-change="selectionChangeHandle"
			@sort-change="sortChangeHandle"
		>
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
	    <#list gridList as field>
			<el-table-column prop="${field.attrName}" label="${field.fieldComment!}" show-overflow-tooltip header-align="center" align="center" <#if field.queryFormType == 'select' || field.queryFormType == 'radio' || field.queryFormType == 'checkbox'>:formatter="(_: any, __: any, value: any) => getLabel(value, ${field.enumNameAllUpper}_ENUM)"</#if> <#if field.gridSort == 1>sortable="custom"</#if></el-table-column>
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
import AddOrUpdate from '@/views/functionNameKebabCase/add-or-update.vue'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
<#list fieldList as field>
<#if field.queryFormType == 'select' || field.queryFormType == 'radio' || field.queryFormType == 'checkbox'>
import { getLabel } from '@/utils/enum'
import { ${field.enumNameAllUpper}_ENUM } from '@/enums/${field.enumName}-enum'
</#if>
</#list>
<#list gridList as field>
	<#if field.queryFormType == 'select' || field.queryFormType == 'radio' || field.queryFormType == 'checkbox'>
import { getLabel } from '@/utils/enum'
import { ${field.enumNameAllUpper}_ENUM } from '@/enums/${field.enumName}-enum'
	</#if>
</#list>
import { ${functionName}EntityPageApi, ${functionName}DeleteListApi } from '@/api/${functionNameKebabCase}'

const state: IHooksOptions = reactive({
    dataListApi: ${functionName}EntityPageApi,
    deleteListApi: ${functionName}DeleteListApi,
    queryForm: {
        <#list queryList as field>
        	<#if field.queryFormType == 'date'>
        dateRange: ''<#sep>, </#sep>
        	<#elseif field.queryFormType == 'datetime'>
		dateTimeRange: []<#sep>, </#sep>
        	<#else>
        ${field.attrName}: ''<#sep>, </#sep>
        	</#if>
        </#list>
    }
})

const queryRef = ref()
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id?: number) => {
	addOrUpdateRef.value.init(id)
}
const resetQueryRef = () => {
  queryRef.value.resetFields()
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle, sortChangeHandle } = useCrud(state)
</script>
