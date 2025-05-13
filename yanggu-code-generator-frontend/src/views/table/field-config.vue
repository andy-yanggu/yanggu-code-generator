<template>
	<el-drawer v-model="visible" title="编辑" :size="1200" :with-header="false">
		<el-tabs v-model="activeName">
			<el-tab-pane label="属性设置" name="field">
				<el-table ref="fieldTable" border :row-key="id" class="sortable-row-gen" :data="fieldList" :row-class-name="tableRowClassName">
					<el-table-column type="index" width="60" label="序号"></el-table-column>
					<el-table-column prop="fieldName" label="字段名"></el-table-column>
					<el-table-column prop="fieldComment" label="说明">
						<template #default="{ row }">
							<el-input v-model="row.fieldComment"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="fieldType" label="字段类型"></el-table-column>
					<el-table-column prop="attrName" label="属性名">
						<template #default="{ row }">
							<el-input v-model="row.attrName"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="attrType" label="属性类型">
						<template #default="{ row }">
							<el-select v-model="row.attrType">
								<el-option v-for="item in typeList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
					<el-table-column prop="autoFill" label="自动填充">
						<template #default="{ row }">
							<el-select v-model="row.autoFill">
								<el-option v-for="item in fillList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
					<el-table-column prop="primaryPk" label="主键">
						<template #default="{ row }">
							<el-checkbox v-model="row.primaryPk"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="logicDelete" label="逻辑删除">
						<template #default="{ row }">
							<el-checkbox v-model="row.logicDelete"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="logicDeleteValue" label="逻辑删除值" width="110">
						<template #default="{ row }">
							<el-input v-model="row.logicDeleteValue" placeholder="输入逻辑删除值"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="logicNotDeleteValue" label="逻辑未删除值" width="110">
						<template #default="{ row }">
							<el-input v-model="row.logicNotDeleteValue" placeholder="输入逻辑未删除值"></el-input>
						</template>
					</el-table-column>
					<!--					<el-table-column prop="dict" label="字典">-->
					<!--						<template #default="{ row }">-->
					<!--							<el-checkbox v-model="row.dict"></el-checkbox>-->
					<!--						</template>-->
					<!--					</el-table-column>-->
					<!--					<el-table-column prop="dictValue" label="字典值" width="110">-->
					<!--						<template #default="{ row }">-->
					<!--							<el-input v-model="row.logicNotDeleteValue" placeholder="输入字典值"></el-input>-->
					<!--						</template>-->
					<!--					</el-table-column>-->
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="查询配置" name="query">
				<el-table ref="queryTable" border :row-key="id" :data="fieldList" :row-class-name="tableRowClassName">
					<el-table-column type="index" width="60" label="序号"></el-table-column>
					<el-table-column prop="attrName" label="属性名"></el-table-column>
					<el-table-column prop="fieldComment" label="说明"></el-table-column>
					<el-table-column prop="queryItem" label="查询显示">
						<template #default="{ row }">
							<el-checkbox v-model="row.queryItem"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="queryType" label="查询方式">
						<template #default="{ row }">
							<el-select v-model="row.queryType">
								<el-option v-for="item in queryList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
					<el-table-column prop="queryFormType" label="查询表单类型">
						<template #default="{ row }">
							<el-select v-model="row.queryFormType">
								<el-option v-for="item in formTypeList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="表单配置" name="form">
				<el-table ref="formTable" border :row-key="id" :data="fieldList" :row-class-name="tableRowClassName">
					<el-table-column type="index" width="60" label="序号"></el-table-column>
					<el-table-column prop="attrName" label="属性名"></el-table-column>
					<el-table-column prop="fieldComment" label="说明"></el-table-column>
					<el-table-column prop="formItem" label="表单显示">
						<template #default="{ row }">
							<el-checkbox v-model="row.formItem"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="formRequired" label="表单必填">
						<template #default="{ row }">
							<el-checkbox v-model="row.formRequired"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="formValidator" label="表单效验">
						<template #default="{ row }">
							<el-input v-model="row.formValidator"></el-input>
						</template>
					</el-table-column>
					<el-table-column prop="formType" label="表单类型">
						<template #default="{ row }">
							<el-select v-model="row.formType">
								<el-option v-for="item in formTypeList" :key="item.value" :value="item.value" :label="item.label"></el-option>
							</el-select>
						</template>
					</el-table-column>
					<el-table-column prop="formDict" label="表单字典类型">
						<template #default="{ row }">
							<el-input v-model="row.formDict"></el-input>
						</template>
					</el-table-column>
				</el-table>
			</el-tab-pane>
			<el-tab-pane label="列表配置" name="grid">
				<el-table ref="gridTable" border :row-key="id" :data="fieldList" :row-class-name="tableRowClassName">
					<el-table-column type="index" width="60" label="序号"></el-table-column>
					<el-table-column prop="attrName" label="属性名"></el-table-column>
					<el-table-column prop="fieldComment" label="说明"></el-table-column>
					<el-table-column prop="gridItem" label="列表显示">
						<template #default="{ row }">
							<el-checkbox v-model="row.gridItem"></el-checkbox>
						</template>
					</el-table-column>
					<el-table-column prop="gridSort" label="列表排序">
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
import { ElMessage, TabsPaneContext } from 'element-plus/es'
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
