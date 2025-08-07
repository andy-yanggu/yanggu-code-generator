<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
	    <#list formList as field>
            <el-form-item label="${field.fieldComment!}" prop="${field.attrName}">
			<#if field.formType == 'text'>
                <el-input v-model="dataForm.${field.attrName}" clearable placeholder="请输入${field.fieldComment!}"></el-input>
			<#elseif field.formType == 'textarea'>
                <el-input type="textarea" v-model="dataForm.${field.attrName}"></el-input>
			<#elseif field.formType == 'editor'>
                <el-input type="textarea" v-model="dataForm.${field.attrName}"></el-input>
			<#elseif field.formType == 'select'>
                <el-select v-model="dataForm.${field.attrName}" clearable filterable placeholder="请选择${field.fieldComment!}">
                    <el-option v-for="item in ${field.enumNameAllUpper}_ENUM" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
			<#elseif field.formType == 'radio'>
                <el-radio-group v-model="dataForm.${field.attrName}">
                    <el-radio v-for="item in ${field.enumNameAllUpper}_ENUM" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                </el-radio-group>
			<#elseif field.formType == 'checkbox'>
                <el-checkbox-group v-model="dataForm.${field.attrName}">
                    <el-checkbox v-for="item in ${field.enumNameAllUpper}_ENUM" :key="item.value" :label="item.label" :value="item.value">{{ item.label }}</el-checkbox>
                </el-checkbox-group>
			<#elseif field.formType == 'date'>
                <el-date-picker type="date" placeholder="请选择${field.fieldComment!}" v-model="dataForm.${field.attrName}"></el-date-picker>
			<#elseif field.formType == 'datetime'>
                <el-date-picker type="datetime" placeholder="请选择${field.fieldComment!}" v-model="dataForm.${field.attrName}"></el-date-picker>
            <#elseif field.formType == 'number'>
                <el-input-number v-model="dataForm.${field.attrName}" size="small"></el-input-number>
			<#else>
                <el-input v-model="dataForm.${field.attrName}" clearable placeholder="请输入${field.fieldComment!}"></el-input>
			</#if>
            </el-form-item>
	    </#list>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ${functionName}DetailApi, ${functionName}SubmitApi } from '@/api/${functionNameKebabCase}'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'
<#list formList as field>
    <#if field.formType == 'select' || field.formType == 'checkbox' || field.formType == 'radio'>
import { ${field.enumNameAllUpper}_ENUM } from '@/enums/${field.enumName}-enum'
    </#if>
</#list>

const emit = defineEmits(['refreshDataList'])

const state: FormOptions = reactive({
    // 提交API
    submitApi: ${functionName}SubmitApi,
    // 详情API
    detailApi: ${functionName}DetailApi,
    // 表单数据
    initFormData: {
        <#list fieldList as field>
            <#if field.entityBaseField == 0>
        ${field.attrName}: ''<#if field_has_next>,</#if>
            </#if>
        </#list>
    },
    emit
})

const dataRules = reactive({
    <#list formList as field>
        <#if field.formRequired == 1>
    ${field.attrName}: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]<#if field_has_next>,</#if>
        </#if>
    </#list>
})

const { visible, dataForm, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
