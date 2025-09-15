<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="${projectPackage}.${projectNameDot}.mapper.${classNameUpper}Mapper">

    <!-- 所有字段 -->
    <sql id="allColumns">
        <#list fieldList as field>
        ${field.fieldName}<#if field_has_next>,</#if>
        </#list>
    </sql>

    <!-- 过滤SQL -->
    <sql id="whereSQL">
        <!-- 这里写过滤语句，过滤项需要加query前缀 -->
        <#list queryList as field>
        <if test="@${projectPackage}.${projectNameDot}.common.mybatis.util.MybatisUtil@isNotEmpty(query, '${field.attrName}')">
            <#if field.queryType == '='>
            AND ${field.fieldName} = <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
            <#elseif field.queryType == '!='>
            AND ${field.fieldName} != <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
            <#elseif field.queryType == '>'>
            AND ${field.fieldName} &gt; <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
            <#elseif field.queryType == '>='>
            AND ${field.fieldName} &gt;= <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
            <#elseif field.queryType == '<'>
            AND ${field.fieldName} &lt; <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
            <#elseif field.queryType == '<='>
            AND ${field.fieldName} &lt;= <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
            <#elseif field.queryType == 'like'>
            AND ${field.fieldName} LIKE CONCAT('%', <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>, '%')
            <#elseif field.queryType == 'left like'>
            AND ${field.fieldName} LIKE CONCAT('%', <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>)
            <#elseif field.queryType == 'right like'>
            AND ${field.fieldName} LIKE CONCAT(<#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>, '%')
            <#else>
            </#if>
        </if>
        </#list>
    </sql>

    <!-- Entity类字段映射 -->
    <resultMap type="${projectPackage}.${projectNameDot}.domain.entity.${classNameUpper}Entity" id="${className}Map">
        <#list fieldList as field>
        <#if field.primaryPk == 1>
        <id property="${field.attrName}" column="${field.fieldName}"/>
        <#else>
        <result property="${field.attrName}" column="${field.fieldName}"/>
        </#if>
        </#list>
    </resultMap>
<#function getLogicDeleteField fieldList >
    <#list fieldList as field>
        <#if field.logicDelete?? && field.logicDelete == 1>
            <#return field.fieldName + " = " + field.logicNotDeleteValue>
        </#if>
    </#list>
    <#return "">
</#function>
<#assign logicDeleteFlex = getLogicDeleteField(fieldList)>

    <!-- Entity分页 -->
    <select id="entityPage" resultMap="${className}Map">
        SELECT
            <include refid="allColumns"/>
        FROM
            ${databaseName}.${tableName}
        <where>
            <#if logicDeleteFlex != ''>
            ${logicDeleteFlex}
            </#if>
            <include refid="whereSQL"/>
        </where>
        <include refid="${projectPackage}.${projectNameDot}.common.mybatis.mapper.BaseMapperPlus.orderBySQL"/>
    </select>

    <!-- Entity列表 -->
    <select id="entityList" resultMap="${className}Map">
        SELECT
            <include refid="allColumns"/>
        FROM
            ${databaseName}.${tableName}
        <where>
            <#if logicDeleteFlex != ''>
            ${logicDeleteFlex}
            </#if>
            <include refid="whereSQL"/>
        </where>
        <include refid="${projectPackage}.${projectNameDot}.common.mybatis.mapper.BaseMapperPlus.orderBySQL"/>
    </select>

    <!-- VO分页 -->
    <select id="voPage" resultType="${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO">
        SELECT
            <include refid="allColumns"/>
        FROM
            ${databaseName}.${tableName}
        <where>
            <#if logicDeleteFlex != ''>
            ${logicDeleteFlex}
            </#if>
            <include refid="whereSQL"/>
        </where>
        <include refid="${projectPackage}.${projectNameDot}.common.mybatis.mapper.BaseMapperPlus.orderBySQL"/>
    </select>

    <!-- VO列表 -->
    <select id="voList" resultType="${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO">
        SELECT
            <include refid="allColumns"/>
        FROM
            ${databaseName}.${tableName}
        <where>
            <#if logicDeleteFlex != ''>
            ${logicDeleteFlex}
            </#if>
            <include refid="whereSQL"/>
        </where>
        <include refid="${projectPackage}.${projectNameDot}.common.mybatis.mapper.BaseMapperPlus.orderBySQL"/>
    </select>

</mapper>