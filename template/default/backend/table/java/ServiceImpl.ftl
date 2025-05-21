package ${projectPackage}.${projectNameDot}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
<#if queryList?has_content>
    <#assign needStrUtil = false>
    <#list queryList as field>
        <#if field.queryType?matches("(like|left like|right like)") || field.attrType?matches("(String)")>
            <#assign needStrUtil = true>
        </#if>
    </#list>
    <#if needStrUtil>
import org.dromara.hutool.core.text.StrUtil;
    </#if>
</#if>
import ${projectPackage}.${projectNameDot}.common.domain.vo.PageVO;
import ${projectPackage}.${projectNameDot}.common.exception.BusinessException;
import ${projectPackage}.${projectNameDot}.common.mybatis.util.MybatisUtil;
import ${projectPackage}.${projectNameDot}.mapstruct.${classNameUpper}Mapstruct;
import ${projectPackage}.${projectNameDot}.domain.entity.${classNameUpper}Entity;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}VOQuery;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}EntityQuery;
import ${projectPackage}.${projectNameDot}.domain.dto.${classNameUpper}DTO;
import ${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO;
import ${projectPackage}.${projectNameDot}.mapper.${classNameUpper}Mapper;
import ${projectPackage}.${projectNameDot}.service.${classNameUpper}Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
<#if queryList?has_content>
    <#assign needStrUtil = false>
    <#list queryList as field>
        <#if field.queryType?matches("(=|!=|>|<|>=|<=)")>
            <#assign needStrUtil = true>
        </#if>
    </#list>
    <#if needStrUtil>
import java.util.Objects;
    </#if>
</#if>

import static ${projectPackage}.${projectNameDot}.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * ${tableComment}Service实现类
 */
@Service
public class ${classNameUpper}ServiceImpl extends ServiceImpl<${classNameUpper}Mapper, ${classNameUpper}Entity> implements ${classNameUpper}Service {

    @Autowired
    private ${classNameUpper}Mapper ${className}Mapper;

    @Autowired
    private ${classNameUpper}Mapstruct ${className}Mapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(${classNameUpper}DTO dto) {
        ${classNameUpper}Entity entity = ${className}Mapstruct.dtoToEntity(dto);
        //唯一性校验等
        ${className}Mapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(${classNameUpper}DTO dto) {
        ${classNameUpper}Entity formEntity = ${className}Mapstruct.dtoToEntity(dto);
        ${classNameUpper}Entity dbEntity = selectById(dto.getId());
        //唯一性校验等
        ${className}Mapper.updateById(formEntity);
    }
<#function getPrimaryKeyType fieldList>
    <#list fieldList as field>
        <#if field.primaryPk == 1>
            <#return field.attrType>
        </#if>
    </#list>
    <#return Long>
</#function>
<#assign primaryKeyType = getPrimaryKeyType(fieldList)>

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(${primaryKeyType} id) {
        ${classNameUpper}Entity dbEntity = selectById(id);
        //删除校验和关联删除
        ${className}Mapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<${primaryKeyType}> idList) {
        //删除校验和关联删除
        ${className}Mapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public ${classNameUpper}VO detail(${primaryKeyType} id) {
        ${classNameUpper}Entity dbEntity = selectById(id);
        return ${className}Mapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<${classNameUpper}VO> entityPage(${classNameUpper}EntityQuery query) {
        //简单sql使用QueryWrapper
        ${className}Mapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //${className}Mapper.entityPage(query);
        return ${className}Mapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<${classNameUpper}VO> entityList(${classNameUpper}EntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<${classNameUpper}Entity> entityList = ${className}Mapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<${classNameUpper}Entity> entityList = ${className}Mapper.entityList(query);
        return ${className}Mapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<${classNameUpper}VO> voPage(${classNameUpper}VOQuery query) {
        ${className}Mapper.voPage(query);
        return ${className}Mapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<${classNameUpper}VO> voList(${classNameUpper}VOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return ${className}Mapper.voList(query);
    }

    /**
     * 批量查询
     */
    @Override
    public List<${classNameUpper}VO> detailList(List<${primaryKeyType}> idList) {
        List<${classNameUpper}Entity> entityList = ${className}Mapper.selectByIds(idList);
        return ${className}Mapstruct.entityToVO(entityList);
    }

    private ${classNameUpper}Entity selectById(${primaryKeyType} id) {
        ${classNameUpper}Entity entity = ${className}Mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "${tableComment}", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<${classNameUpper}Entity> buildQueryWrapper(${classNameUpper}EntityQuery query) {
        LambdaQueryWrapper<${classNameUpper}Entity> wrapper = Wrappers.lambdaQuery(${classNameUpper}Entity.class);
        <#if queryList?has_content>

        //过滤字段
        <#list queryList as field>
            <#if field.queryType == '='>
                <#if field.attrType == 'String'>
        wrapper.eq(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.eq(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == '!='>
                <#if field.attrType == 'String'>
        wrapper.ne(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.ne(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == '>'>
                <#if field.attrType == 'String'>
        wrapper.gt(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.gt(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == '>='>
                <#if field.attrType == 'String'>
        wrapper.ge(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.ge(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == '<'>
                <#if field.attrType == 'String'>
        wrapper.lt(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.lt(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == '<='>
                <#if field.attrType == 'String'>
        wrapper.le(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.le(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == 'like'>
                <#if field.attrType == 'String'>
        wrapper.like(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.like(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == 'left like'>
                <#if field.attrType == 'String'>
        wrapper.likeLeft(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.likeLeft(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            <#elseif field.queryType == 'right like'>
                <#if field.attrType == 'String'>
        wrapper.likeRight(StrUtil.isNotBlank(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                <#else>
        wrapper.likeRight(Objects.nonNull(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
                </#if>
            </#if>
        </#list>
        </#if>

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}