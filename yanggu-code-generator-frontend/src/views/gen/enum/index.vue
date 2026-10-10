<template>
	<div>
		<query-table :table-hook="tableHook">
			<!-- 工具栏左侧 -->
			<template #toolbar-left>
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
					<el-button type="danger" :loading="tableHook.action.delete.loading" :icon="Delete" @click="tableHook.action.delete()">删除</el-button>
					<el-button type="success" :icon="DocumentAdd" @click="generatorBatchHandler()">生成</el-button>
				</el-space>
			</template>

			<!-- 查询字段插槽：项目选择器 -->
			<template #query-projectId>
				<el-select v-model="tableHook.search.form.projectId" style="width: 180px" placeholder="请选择项目" clearable filterable>
					<el-option v-for="projectItem in projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
						<option-label :label="projectItem.projectName" :desc="projectItem.projectDesc"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表格列插槽：枚举项数量 -->
			<template #table-enumItemCount="{ row }">
				<el-tooltip content="配置枚举项" placement="top">
					<el-button type="primary" link @click="configEnumItemHandle(row)">{{ row.enumItemCount }}个</el-button>
				</el-tooltip>
			</template>

			<!-- 操作列 -->
			<template #operation="{ row }">
				<el-row :gutter="5">
					<el-col :span="12">
						<el-button type="primary" link :icon="Setting" @click="configEnumItemHandle(row)">配置</el-button>
					</el-col>
					<el-col :span="12">
						<el-button type="primary" link :icon="Edit" @click="formInitHandle(row.id)">修改</el-button>
					</el-col>
				</el-row>
				<el-row :gutter="5">
					<el-col :span="12">
						<el-button type="primary" link :icon="View" @click="previewHandle(row)">预览</el-button>
					</el-col>
					<el-col :span="12">
						<el-dropdown>
							<el-button type="primary" link :icon="More">更多</el-button>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item :icon="DocumentAdd" @click="generatorHandler(row)">生成</el-dropdown-item>
									<el-dropdown-item :icon="Delete" @click="tableHook.action.delete(row)">删除</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</el-col>
				</el-row>
			</template>
		</query-table>

		<!-- 弹窗, 新增 / 修改 -->
		<enum-form ref="formRef" @refresh-data-list="tableHook.query.getDataList()"></enum-form>

		<enum-item-index ref="enumItemIndexRef" @refresh-data-list="tableHook.query.getDataList()"></enum-item-index>

		<preview ref="previewRef"></preview>

		<template-index ref="templateIndexRef" @clear-selection="clearSelectionHandler()"></template-index>
	</div>
</template>

<script setup lang="ts">
import EnumForm from '@/views/gen/enum/form.vue'
import EnumItemIndex from '@/views/gen/enum-item/index.vue'
import TemplateIndex from '@/views/gen/enum/template-index.vue'
import Preview from '@/business/preview/index.vue'
import { genEnumApi, genProjectApi } from '@/api'
import { GenEnumEntity, GenEnumQuery, GenProjectEntity } from '@/types'
import { getLabel } from '@/utils/enum'
import { GENERATOR_PRODUCT_TYPES, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import {
	createBuiltinCreateTime,
	createBuiltinIndex,
	createBuiltinOperation,
	createBuiltinSelection,
	createBuiltinUpdateTime,
	useCrudTable,
	useInitForm
} from '@/hooks'
import { Delete, DocumentAdd, Edit, More, Plus, Setting, View } from '@element-plus/icons-vue'
import QueryTable from '@/components/crud/QueryTable.vue'
import OptionLabel from '@/components/option/label/index.vue'

defineOptions({
	name: 'GenEnum'
})

onMounted(() => {
	getProjectList()
})

const enumItemIndexRef = ref()
const previewRef = ref()
const templateIndexRef = ref()
const projectList = ref([] as GenProjectEntity[])

const getProjectList = () => {
	genProjectApi.entityList().then(data => {
		projectList.value = data
	})
}

const configEnumItemHandle = ({ id, enumName }: { id: string; enumName: string }) => {
	enumItemIndexRef.value.init(id, enumName)
}

const previewHandle = (row: any) => {
	previewRef.value.init(row.id, row.enumName, row.projectId, row.generatorType, GENERATOR_PRODUCT_TYPES.Enum)
}

const generatorBatchHandler = () => {
	const data = tableHook.selection.rows
	if (data.length === 0) {
		ElMessage.warning('请选择要生成代码的枚举')
		return
	}
	genEnumApi.generateCheck(tableHook.selection.ids).then(resData => {
		const { checkResult, enumTemplateGroupId, generatorType } = resData
		if (!checkResult) {
			ElMessage.warning('当前选择的枚举不是同一个项目')
		} else {
			templateIndexRef.value.init(enumTemplateGroupId, generatorType, data)
		}
	})
}

const clearSelectionHandler = () => {
	tableHook.selection.clear()
}

const generatorHandler = (row: any) => {
	templateIndexRef.value.init(row.enumTemplateGroupId, row.generatorType, [row.id])
}

// ====== 表格配置 ======
const tableHook = useCrudTable<GenEnumEntity, GenEnumQuery>({
	subject: '枚举',
	deleteNameKey: 'enumName',
	dataListApi: genEnumApi.voPage,
	deleteApi: genEnumApi.deleteList,
	initQueryForm: (): GenEnumQuery => ({
		enumName: '',
		projectId: ''
	}),
	searchSchema: [
		{ prop: 'enumName', label: '枚举名称' },
		{ prop: 'projectId', label: '项目' }
	],
	tableColumns: [
		createBuiltinSelection(),
		createBuiltinIndex(),
		{ key: 'projectName', label: '项目名称' },
		{ key: 'enumName', label: '枚举名称' },
		{ key: 'generatorType', label: '生成类型', formatter: getLabel(PROJECT_GENERATE_TYPES) },
		{ key: 'enumDesc', label: '枚举描述' },
		{ key: 'enumItemCount', label: '枚举项数量' },
		createBuiltinCreateTime(),
		createBuiltinUpdateTime(),
		createBuiltinOperation()
	]
})

const { formRef, formInitHandle } = useInitForm()
</script>

<style scoped>
:deep(.el-dropdown) {
	vertical-align: middle;
}
</style>
