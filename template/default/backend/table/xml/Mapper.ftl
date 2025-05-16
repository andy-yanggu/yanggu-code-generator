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
            <#if field.queryType == '='>
        <if test="query.${field.attrName} != null<#if field.attrType == 'String'> and query.${field.attrName}.trim() != ''</#if>">
            AND ${field.fieldName} = <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
        </if>
            <#elseif field.queryType == '!='>
        <if test="query.${field.attrName} != null<#if field.attrType == 'String'> and query.${field.attrName}.trim() != ''</#if>">
            AND ${field.fieldName} != <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
        </if>
            <#elseif field.queryType == '>'>
        <if test="query.${field.attrName} != null<#if field.attrType == 'String'> and query.${field.attrName}.trim() != ''</#if>">
            AND ${field.fieldName} &gt; <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
        </if>
            <#elseif field.queryType == '>='>
        <if test="query.${field.attrName} != null<#if field.attrType == 'String'> and query.${field.attrName}.trim() != ''</#if>">
            AND ${field.fieldName} &gt;= <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
        </if>
            <#elseif field.queryType == '<'>
        <if test="query.${field.attrName} != null<#if field.attrType == 'String'> and query.${field.attrName}.trim() != ''</#if>">
            AND ${field.fieldName} &lt; <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
        </if>
            <#elseif field.queryType == '<='>
        <if test="query.${field.attrName} != null<#if field.attrType == 'String'> and query.${field.attrName}.trim() != ''</#if>">
            AND ${field.fieldName} &lt;= <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>
        </if>
            <#elseif field.queryType == 'like'>
        <if test="query.${field.attrName} != null and query.${field.attrName}.trim() != ''">
            AND ${field.fieldName} LIKE CONCAT('%', <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>, '%')
        </if>
            <#elseif field.queryType == 'left like'>
        <if test="query.${field.attrName} != null and query.${field.attrName}.trim() != ''">
            AND ${field.fieldName} LIKE CONCAT('%', <#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>)
        </if>
            <#elseif field.queryType == 'right like'>
        <if test="query.${field.attrName} != null and query.${field.attrName}.trim() != ''">
            AND ${field.fieldName} LIKE CONCAT(<#noparse>#{query.</#noparse>${field.attrName}<#noparse>}</#noparse>, '%')
        </if>
            <#else>
            </#if>
        </#list>
    </sql>

    <!-- 排序SQL -->
    <sql id="orderBySQL">
        <if test="query.orders != null and query.orders.size() > 0">
            ORDER BY
            <foreach collection="query.orders" item="order" separator=",">
                <#noparse>${order.column}</#noparse> <if test="order.isAsc">ASC</if><if test="!order.isAsc">DESC</if>
            </foreach>
        </if>
    </sql>

    <!-- Entity类字段映射 -->
    <resultMap type="${projectPackage}.${projectNameDot}.domain.entity.${classNameUpper}Entity" id="${className}Map">
        <#list fieldList as field>
        <#if field.primaryPk>
        <id property="${field.attrName}" column="${field.fieldName}"/>
        <#else>
        <result property="${field.attrName}" column="${field.fieldName}"/>
        </#if>
        </#list>
    </resultMap>
<#function getLogicDeleteField fieldList >
    <#list fieldList as field>
        <#if field.logicDelete?? && field.logicDelete>
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
        <include refid="orderBySQL"/>
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
        <include refid="orderBySQL"/>
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
        <include refid="orderBySQL"/>
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
        <include refid="orderBySQL"/>
    </select>

</mapper>