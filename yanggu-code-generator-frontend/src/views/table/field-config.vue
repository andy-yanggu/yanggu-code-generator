<template>
	<el-drawer v-model="visible" title="编辑" :size="1200" :with-header="false">
		<el-tabs v-model="activeName">
			<el-tab-pane label="属性设置" name="field">
				<el-table
					ref="fieldTable"
					border
					:row-key="id"
					class="sortable-row-gen"
					:data="getFieldListData(0)"
					:row-class-name="tableRowClassName"
					:show-overflow-tooltip="true"
				>
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldName" show-overflow-tooltip label="字段名" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldComment" label="说明" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.fieldComment"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="fieldType" label="字段类型" header-align="center" align="center"></el-table-column>
					<el-table-column prop="attrName" label="属性名" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.attrName"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="attrType" label="属性类型" width="120" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.attrType">
								<el-option v-for="item in typeList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
					<el-table-column prop="autoFill" label="自动填充" width="140" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.autoFill">
								<el-option v-for="item in fillList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
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
								<el-checkbox v-model="row.primaryPk"></el-checkbox>
							</div>
						</template>
					</el-table-column>
					<el-table-column prop="logicDelete" label="逻辑删除" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.logicDelete"></el-checkbox>
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
					<el-table-column prop="dict" label="字典" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.dict"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="dictValue" label="字典值" width="110" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.dictValue" placeholder="例如：0-男、1-女"></el-input>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="查询配置" name="query">
				<el-table ref="queryTable" border :row-key="id" :data="getFieldListData(1)" :row-class-name="tableRowClassName">
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="attrName" label="属性名" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldComment" label="说明" header-align="center" align="center"></el-table-column>
					<el-table-column prop="queryItem" label="查询显示" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.queryItem"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="queryFieldSort" label="字段排序" width="150" header-align="center" align="center">
						<template #default="{ row }">
							<el-input-number v-model="row.queryFieldSort" :min="0" :max="fieldList.length - 1" size="small"></el-input-number>
						</template>
					</el-table-column>
					<el-table-column prop="queryType" label="查询方式" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.queryType">
								<el-option v-for="item in queryList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
					<el-table-column prop="queryFormType" label="查询表单类型" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.queryFormType">
								<el-option v-for="item in formTypeList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="表单配置" name="form">
				<el-table ref="formTable" border :row-key="id" :data="getFieldListData(2)" :row-class-name="tableRowClassName">
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="attrName" label="属性名" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldComment" label="说明" header-align="center" align="center"></el-table-column>
					<el-table-column prop="formItem" label="表单显示" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.formItem"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="formFieldSort" label="字段排序" width="150" header-align="center" align="center">
						<template #default="{ row }">
							<el-input-number v-model="row.formFieldSort" :min="0" :max="fieldList.length - 1" size="small"></el-input-number>
						</template>
					</el-table-column>
					<el-table-column prop="formRequired" label="表单必填" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.formRequired"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="formValidator" label="表单效验" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.formValidator"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="formType" label="表单类型" header-align="center" align="center">
						<template #default="{ row }">
							<el-select v-model="row.formType">
								<el-option v-for="item in formTypeList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
					<el-table-column prop="formDict" label="表单字典类型" header-align="center" align="center">
						<template #default="{ row }">
							<el-input v-model="row.formDict"></el-input>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="列表配置" name="grid">
				<el-table ref="gridTable" border :row-key="id" :data="getFieldListData(3)" :row-class-name="tableRowClassName">
					<el-table-column type="index" width="60" label="序号" header-align="center" align="center"></el-table-column>
					<el-table-column prop="attrName" label="属性名" header-align="center" align="center"></el-table-column>
					<el-table-column prop="fieldComment" label="说明" header-align="center" align="center"></el-table-column>
					<el-table-column prop="gridItem" label="列表显示" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.gridItem"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="gridFieldSort" label="字段排序" width="150" header-align="center" align="center">
						<template #default="{ row }">
							<el-input-number v-model="row.gridFieldSort" :min="0" :max="fieldList.length - 1" size="small"></el-input-number>
						</template>
					</el-table-column>
					<el-table-column prop="gridSort" label="列表排序" header-align="center" align="center">
						<template #default="{ row }">
							<el-checkbox v-model="row.gridSort"></el-checkbox>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
		</el-tabs>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-drawer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { tableFieldSubmitListApi, tableFieldEntityListApi } from '@/api/tableField'
import { fieldTypeListApi } from '@/api/fieldType'

const activeName = ref()
const fieldTable = ref()
const formTable = ref()
const gridTable = ref()
const queryTable = ref()

const emit = defineEmits(['refreshDataList'])
const visible = ref(false)
const dataFormRef = ref()
const typeList = ref([]) as any
const tableId = ref()
const fieldList = ref([])
const fillList = reactive([
	{ label: 'DEFAULT', value: 'DEFAULT' },
	{ label: 'INSERT', value: 'INSERT' },
	{ label: 'UPDATE', value: 'UPDATE' },
	{ label: 'INSERT_UPDATE', value: 'INSERT_UPDATE' }
])

const queryList = reactive([
	{ label: '=', value: '=' },
	{ label: '!=', value: '!=' },
	{ label: '>', value: '>' },
	{ label: '>=', value: '>=' },
	{ label: '<', value: '<' },
	{ label: '<=', value: '<=' },
	{ label: 'like', value: 'like' },
	{ label: 'left like', value: 'left like' },
	{ label: 'right like', value: 'right like' }
])

const formTypeList = reactive([
	{ label: '单行文本', value: 'text' },
	{ label: '多行文本', value: 'textarea' },
	{ label: '富文本编辑器', value: 'editor' },
	{ label: '下拉框', value: 'select' },
	{ label: '单选按钮', value: 'radio' },
	{ label: '复选框', value: 'checkbox' },
	{ label: '日期', value: 'date' },
	{ label: '日期时间', value: 'datetime' }
])

const init = (id: number) => {
	visible.value = true
	tableId.value = id

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	activeName.value = 'field'

	getTable(id)
	getFieldTypeList()
}

const getTable = (id: number) => {
	const queryForm = {
		tableId: id
	}
	tableFieldEntityListApi(queryForm).then(res => {
		fieldList.value = res.data
	})
}

const getFieldListData = (type: number): any[] => {
	const list = [...fieldList.value]
	if (type === 1) {
		return list.sort((a, b) => a.fieldSort - b.fieldSort)
	} else if (type === 2) {
		return list.sort((a, b) => a.queryFieldSort - b.queryFieldSort)
	} else if (type === 3) {
		return list.sort((a, b) => a.formFieldSort - b.formFieldSort)
	} else {
		return list.sort((a, b) => a.gridFieldSort - b.gridFieldSort)
	}
}

const getFieldTypeList = async () => {
	typeList.value = []

	// 获取数据
	const { data } = await fieldTypeListApi()
	// 设置属性类型值
	data.forEach((item: any) => typeList.value.push({ label: item, value: item }))
	// 增加Object类型
	typeList.value.push({ label: 'Object', value: 'Object' })
}

// 表单提交
const submitHandle = () => {
	tableFieldSubmitListApi(fieldList.value).then(() => {
		ElMessage.success({
			message: '操作成功',
			duration: 500,
			onClose: () => {
				visible.value = false
				emit('refreshDataList')
			}
		})
	})
}

defineExpose({
	init
})

// 添加表格行样式
const tableRowClassName = ({ row, rowIndex }: { row: any; rowIndex: number }) => {
	if (rowIndex === 0) {
		return 'sortable-ghost'
	}
	return ''
}
</script>

<style lang="scss">
.sortable-row-gen .drag-btn {
	cursor: move;
	font-size: 12px;
}

.sortable-row-gen .el-table__row.sortable-ghost,
.sortable-row-gen .el-table__row.sortable-chosen {
	background-color: #dfecfb;
}
</style>
