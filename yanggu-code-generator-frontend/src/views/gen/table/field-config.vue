<template>
	<el-dialog v-model="visible" title="字段配置" width="80%" class="field-config-dialog" @close="visible = false">
		<el-tabs v-model="activeName">
			<el-tab-pane label="属性配置" name="field">
				<el-table
					ref="fieldTable"
					border
					row-key="id"
					max-height="60vh"
					class="layout-table"
					header-cell-class-name="layout-table-header"
					:data="getFieldListData(0)"
					:show-overflow-tooltip="true"
				>
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldName" show-overflow-tooltip label="字段名称" header-align="center" align="center" width="100"></el-table-column>
					<el-table-column prop="fieldComment" label="注释" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.fieldComment"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="fieldType" label="字段类型" header-align="center" align="center" width="100"></el-table-column>
					<el-table-column prop="attrName" label="属性名称" header-align="center" align="center" width="100">
						<template #default="{ row }">
							<el-input v-model="row.attrName"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="attrType" label="属性类型" width="120" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.attrType" :options="typeList"></el-select>
						</template>
					</el-table-column>
					<el-table-column prop="autoFill" label="自动填充" width="140" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.autoFill" :options="fillList"></el-select>
						</template>
					</el-table-column>
					<el-table-column prop="fieldSort" label="字段排序" width="150" header-align="center" align="center">
						<template #default="{ row }">
							<el-input-number v-model="row.fieldSort" :min="0" :max="fieldList.length - 1" size="small"></el-input-number>
						</template>
					</el-table-column>
					<el-table-column prop="primaryPk" label="主键" header-align="center" align="center">
						<template #default="{ row }">
							<div style="display: flex; justify-content: center">
								<el-checkbox v-model="row.primaryPk" :true-value="1" :false-value="0"></el-checkbox>
							</div>
						</template>
					</el-table-column>
					<el-table-column prop="enumId" label="枚举配置" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-select
								v-model="row.enumId"
								:options="enumList"
								:props="{ label: 'enumName', value: 'id' }"
								filterable
								placeholder="请选择枚举"
								clearable
							></el-select>
						</template>
					</el-table-column>
					<el-table-column prop="uniqueField" label="唯一性" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.uniqueField" :true-value="1" :false-value="0"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="logicDelete" label="逻辑删除" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.logicDelete" :true-value="1" :false-value="0"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="logicDeleteValue" label="逻辑删除值" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.logicDeleteValue" placeholder="例如：1"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="logicNotDeleteValue" label="逻辑未删除值" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.logicNotDeleteValue" placeholder="例如：0"></el-input>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="查询配置" name="query">
				<el-table ref="queryTable" border row-key="id" :data="getFieldListData(1)" class="layout-table" header-cell-class-name="layout-table-header">
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="attrName" label="属性名称" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldComment" label="注释" header-align="center" align="center"></el-table-column>
					<el-table-column prop="queryItem" label="查询显示" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.queryItem" :true-value="1" :false-value="0"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="queryFieldSort" label="字段排序" width="150" header-align="center" align="center">
						<template #default="{ row }">
							<el-input-number v-model="row.queryFieldSort" :min="0" :max="fieldList.length - 1" size="small"></el-input-number>
						</template>
					</el-table-column>
					<el-table-column prop="queryType" label="查询方式" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.queryType" :options="queryList"></el-select>
						</template>
					</el-table-column>
					<el-table-column prop="queryFormType" label="查询表单类型" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.queryFormType" :options="formTypeList"></el-select>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="表单配置" name="form">
				<el-table ref="formTable" border row-key="id" class="layout-table" header-cell-class-name="layout-table-header" :data="getFieldListData(2)">
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="attrName" label="属性名称" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldComment" label="注释" header-align="center" align="center"></el-table-column>
					<el-table-column prop="formItem" label="表单显示" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.formItem" :true-value="1" :false-value="0"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="formFieldSort" label="字段排序" width="150" header-align="center" align="center">
						<template #default="{ row }">
							<el-input-number v-model="row.formFieldSort" :min="0" :max="fieldList.length - 1" size="small"></el-input-number>
						</template>
					</el-table-column>
					<el-table-column prop="formRequired" label="表单必填" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.formRequired" :true-value="1" :false-value="0"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="formValidator" label="表单效验" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.formValidator" :options="formValidatorList"></el-select>
						</template>
					</el-table-column>
					<el-table-column prop="formType" label="表单类型" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.formType" :options="formTypeList"></el-select>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="列表配置" name="grid">
				<el-table ref="gridTable" border row-key="id" class="layout-table" header-cell-class-name="layout-table-header" :data="getFieldListData(3)">
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="attrName" label="属性名称" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldComment" label="注释" header-align="center" align="center"></el-table-column>
					<el-table-column prop="gridItem" label="列表显示" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.gridItem" :true-value="1" :false-value="0"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="gridFieldSort" label="字段排序" width="150" header-align="center" align="center">
						<template #default="{ row }">
							<el-input-number v-model="row.gridFieldSort" :min="0" :max="fieldList.length - 1" size="small"></el-input-number>
						</template>
					</el-table-column>
					<el-table-column prop="gridSort" label="列表排序" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.gridSort" :true-value="1" :false-value="0"></el-checkbox>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
		</el-tabs>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle(fieldList)">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { nextTick, ref, shallowReactive } from 'vue'
import { ElMessage } from 'element-plus/es'
import { genEnumApi, genFieldTypeApi, genTableFieldApi } from '@/api'
import { GenTableFieldEntity } from '@/types'
import { Check, Close } from '@element-plus/icons-vue'
import { ElLoading } from 'element-plus'
import { LabelData } from '@/types'
import { SubmitOptions, useSubmitHandler } from '@/hooks/use-submit-handler'

defineOptions({
	name: 'GenTableFieldConfig'
})

const activeName = ref()
const fieldTable = ref()
const formTable = ref()
const gridTable = ref()
const queryTable = ref()

const emit = defineEmits(['refreshDataList'])
const visible = ref(false)
const typeList = ref([] as LabelData[])
const tableId = ref()
const projectIdRef = ref(-1)
const fieldList = ref([] as GenTableFieldEntity[])
const fillList = ref([
	{ label: 'DEFAULT', value: 'DEFAULT' },
	{ label: 'INSERT', value: 'INSERT' },
	{ label: 'UPDATE', value: 'UPDATE' },
	{ label: 'INSERT_UPDATE', value: 'INSERT_UPDATE' }
])
const formValidatorList = ref([
	{ label: 'NotNull', value: 'NotNull' },
	{ label: 'NotBlank', value: 'NotBlank' },
	{ label: 'NotEmpty', value: 'NotEmpty' },
	{ label: 'Null', value: 'Null' }
])
const queryList = ref([
	{ label: '=', value: '=' },
	{ label: '!=', value: '!=' },
	{ label: '>', value: '>' },
	{ label: '>=', value: '>=' },
	{ label: '<', value: '<' },
	{ label: '<=', value: '<=' },
	{ label: 'like', value: 'like' },
	{ label: 'left like', value: 'left like' },
	{ label: 'right like', value: 'right like' },
	{ label: 'between', value: 'between' }
])

const formTypeList = ref([
	{ label: '单行文本', value: 'text' },
	{ label: '多行文本', value: 'textarea' },
	{ label: '富文本编辑器', value: 'editor' },
	{ label: '下拉框', value: 'select' },
	{ label: '单选按钮', value: 'radio' },
	{ label: '复选框', value: 'checkbox' },
	{ label: '日期', value: 'date' },
	{ label: '日期时间', value: 'datetime' },
	{ label: '数值', value: 'number' }
])

const enumList = ref([] as GenEnumEntity[])

const init = async (row: any) => {
	visible.value = true
	const id = row.id
	projectIdRef.value = row.projectId
	tableId.value = id

	activeName.value = 'field'

	// 等待DOM更新后再显示loading
	await nextTick()

	// 局部加载，指定目标为当前对话框
	const loadingInstance = ElLoading.service({
		target: '.field-config-dialog .el-dialog__body',
		text: '数据加载中...'
	})

	try {
		// 并行执行所有异步请求
		const [fieldRes, enumRes, fieldTypeRes] = await Promise.all([
			getTableFieldList(id),
			genEnumApi.entityList({ projectId: projectIdRef.value }),
			genFieldTypeApi.list()
		])

		fieldList.value = fieldRes
		enumList.value = enumRes

		// 设置属性类型值
		fieldTypeRes.forEach((item: string) => {
			typeList.value.push({ label: item, value: item })
		})
		// 增加Object类型
		typeList.value.push({ label: 'Object', value: 'Object' })
	} catch {
		ElMessage.error('数据加载失败')
	} finally {
		loadingInstance.close()
	}
}

const getTableFieldList = (id: number) => {
	const queryForm = {
		tableId: id
	}
	return genTableFieldApi.entityList(queryForm)
}

const getFieldListData = (type: number) => {
	const list = [...fieldList.value]
	if (type === 0) {
		return list.sort((a, b) => a.fieldSort - b.fieldSort)
	} else if (type === 1) {
		return list.sort((a, b) => a.queryFieldSort - b.queryFieldSort)
	} else if (type === 2) {
		return list.sort((a, b) => a.formFieldSort - b.formFieldSort)
	} else if (type === 3) {
		return list.sort((a, b) => a.gridFieldSort - b.gridFieldSort)
	}
}

const state = shallowReactive({
	visible,
	submitApi: genTableFieldApi.submitList,
	message: '字段配置成功',
	emit
} as SubmitOptions)

const { submitLoading, submitHandle } = useSubmitHandler(state)

defineExpose({
	init
})
</script>

<style lang="scss"></style>
