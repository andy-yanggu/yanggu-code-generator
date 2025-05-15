<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
	    <#list formList as field>
            <el-form-item label="${field.fieldComment!}" prop="${field.attrName}">
			<#if field.formType == 'text'>
                <el-input v-model="dataForm.${field.attrName}" placeholder="请输入${field.fieldComment!}"></el-input>
			<#elseif field.formType == 'textarea'>
                <el-input type="textarea" v-model="dataForm.${field.attrName}"></el-input>
			<#elseif field.formType == 'editor'>
                <el-input type="textarea" v-model="dataForm.${field.attrName}"></el-input>
			<#elseif field.formType == 'select'>
                <el-select v-model="dataForm.${field.attrName}" clearable placeholder="请选择${field.fieldComment!}">
                    <el-option v-for="item in ${tableName}_${field.fieldName}_enum" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
			<#elseif field.formType == 'radio'>
                <el-radio-group v-model="dataForm.${field.attrName}">
                    <el-radio v-for="item in ${tableName}_${field.attrName}_enum" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                </el-radio-group>
			<#elseif field.formType == 'checkbox'>
                <el-checkbox-group v-model="dataForm.${field.attrName}">
                    <el-checkbox v-for="item in ${tableName}_${field.fieldName}_enum" :key="item.value" :label="item.label" :value="item.value">{{ item.label }}</el-checkbox>
                </el-checkbox-group>
			<#elseif field.formType == 'date'>
                <el-date-picker type="date" placeholder="请选择${field.fieldComment!}" v-model="dataForm.${field.attrName}"></el-date-picker>
			<#elseif field.formType == 'datetime'>
                <el-date-picker type="datetime" placeholder="请选择${field.fieldComment!}" v-model="dataForm.${field.attrName}"></el-date-picker>
			<#else>
                <el-input v-model="dataForm.${field.attrName}" placeholder="请输入${field.fieldComment!}"></el-input>
			</#if>
            </el-form-item>
	    </#list>
		</el-form>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { ${functionName}DetailApi, ${functionName}SubmitApi } from '@/api/${functionName}'
<#list formList as field>
<#if field.formType == 'select' || field.formType == 'checkbox' || field.formType == 'radio'>
import { ${tableName}_${field.fieldName}_enum } from '@/constant/enum'
</#if>
</#list>

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	<#list fieldList as field>
	    <#if !field.baseField>
	${field.attrName}: ''<#sep>,
	    </#if>
	</#list>
})

const init = (id?: number) => {
	visible.value = true
	dataForm.id = null

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	if (id) {
		get${functionNamePascal}(id)
	}
}

const get${functionNamePascal} = (id: number) => {
	${functionName}DetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = ref({
	<#list formList as field>
	<#if field.formRequired>
	${field.attrName}: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]<#sep>,
	</#if>
	</#list>
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		${functionName}SubmitApi(dataForm).then(() => {
			ElMessage.success({
				message: '操作成功',
				duration: 500,
				onClose: () => {
					visible.value = false
					emit('refreshDataList')
				}
			})
		})
	})
}

defineExpose({
	init
})
</script>
