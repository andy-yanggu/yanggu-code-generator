<template>
	<div>
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
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
					<el-select
            v-model="state.queryForm.${field.attrName}" 
            style="width: 160px" 
            :options="${field.enumNameAllUpper}_ENUM" 
            filterable 
            clearable 
            placeholder="请选择${field.fieldComment}">
          </el-select>
				<#elseif field.queryFormType == 'radio'>
					<el-radio-group v-model="state.queryForm.${field.attrName}" :options="${field.enumNameAllUpper}_ENUM"></el-radio-group>
				<#elseif field.queryFormType == 'checkbox'>
					<el-checkbox-group v-model="state.queryForm.${field.attrName}" :options="${field.enumNameAllUpper}_ENUM"></el-checkbox-group>
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
					></el-date-picker>
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
					></el-date-picker>
				<#else>
					<el-input v-model="state.queryForm.${field.attrName}" placeholder="请输入${field.fieldComment}"></el-input>
				</#if>
				</el-form-item>
			</#list>
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
						<el-space>
							<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
							<el-button type="danger" :loading="state.deleteLoading" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
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
				row-key="id"
				max-height="60vh"
				class="layout-table"
        header-cell-class-name="layout-table-header"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<#list gridList as field>
				<el-table-column prop="${field.attrName}" label="${field.fieldComment!}" show-overflow-tooltip header-align="center" align="center" <#if field.formType == 'select' || field.formType == 'radio' || field.formType == 'checkbox'>:formatter="getLabel(${field.enumNameAllUpper}_ENUM)"</#if> <#if field.gridSort == 1>sortable="custom"</#if>></el-table-column>
			</#list>
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
					<template #default="scope">
						<el-button type="primary" link :icon="Edit" @click="formInitHandle(scope.row.id)">修改</el-button>
						<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row)">删除</el-button>
					</template>
				</el-table-column>
			</el-table>
      <!-- 分页操作栏 -->
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

			<!-- 弹窗表单 -->
			<${moduleName}-${functionNameKebabCase}-form ref="formRef" @refresh-data-list="getDataList()"></${moduleName}-${functionNameKebabCase}-form>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import ${moduleNamePascal}${functionNamePascal}Form from '@/views/${moduleName}/${functionNameKebabCase}/form.vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { useTableAction, useInitForm } from '@/hooks'
import { ${moduleNameCase}${functionNamePascal}Api } from '@/api'
import { IHooksOptions, ${moduleNamePascal}${functionNamePascal}Entity, ${moduleNamePascal}${functionNamePascal}Query } from '@/types'
<#function findEnumName list typeField>
  <#list list as field>
    <#if field[typeField] == 'select'
      || field[typeField] == 'radio'
      || field[typeField] == 'checkbox'>
      <#if field.enumNameAllUpper??>
        <#return field.enumNameAllUpper>
      </#if>
    </#if>
  </#list>
  <#return "">
</#function>
<#assign enumName =
    findEnumName(queryList, "queryFormType")
    ! findEnumName(gridList, "formType")
/>
<#if enumName?has_content>
import { getLabel } from '@/utils/enum'
import { ${enumName}_ENUM } from '@/constant/enum'
</#if>

defineOptions({
	name: '${moduleNamePascal}${functionNamePascal}'
})

// 初始化查询表单数据
const initQueryFormData = (): ${moduleNamePascal}${functionNamePascal}Query => ({
  <#list queryList as field>
    <#if field.queryFormType == 'date'>
  dateRange: []<#sep>, </#sep>
    <#elseif field.queryFormType == 'datetime'>
  dateTimeRange: []<#sep>, </#sep>
    <#elseif field.queryFormType == 'checkbox'>
  ${field.attrName}: []<#sep>, </#sep>
    <#elseif field.queryFormType == 'number'>
  ${field.attrName}: -1<#sep>, </#sep>
    <#else>
  ${field.attrName}: ''<#sep>, </#sep>
    </#if>
  </#list>
})

const state = reactive({
    tableSubject: '${tableComment}',
	  deleteNameKey: '${tableTemplatePropertyData['deleteNameKey']}',
    dataListApi: ${moduleNameCase}${functionNamePascal}Api.page,
    deleteListApi: ${moduleNameCase}${functionNamePascal}Api.deleteList,
    initQueryFormData,
    queryForm: initQueryFormData()
} as IHooksOptions<${moduleNamePascal}${functionNamePascal}Entity, ${moduleNamePascal}${functionNamePascal}Query>)

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
