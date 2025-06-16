package ${projectPackage}.${projectNameDot}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import java.util.Objects;

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
        //唯一性校验等
        //checkUnique(dto);
        ${classNameUpper}Entity entity = ${className}Mapstruct.dtoToEntity(dto);
        ${className}Mapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(${classNameUpper}DTO dto) {
        //唯一性校验等
        //checkUnique(dto);
        ${classNameUpper}Entity formEntity = ${className}Mapstruct.dtoToEntity(dto);
        ${classNameUpper}Entity dbEntity = selectById(dto.getId());
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
        //checkReference(List.of(dbEntity));
        //删除校验和关联删除
        ${className}Mapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<${primaryKeyType}> idList) {
        List<${classNameUpper}Entity> dbEntityList = ${className}Mapper.selectByIds(idList);
        //checkReference(dbEntityList);
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
     * 详情列表
     */
    @Override
    public List<${classNameUpper}VO> detailList(List<${primaryKeyType}> idList) {
        List<${classNameUpper}Entity> entityList = ${className}Mapper.selectByIds(idList);
        return ${className}Mapstruct.entityToVO(entityList);
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
     * ID查询
     */
    private ${classNameUpper}Entity selectById(${primaryKeyType} id) {
        ${classNameUpper}Entity entity = ${className}Mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "${tableComment}", id);
        }
        return entity;
    }

    /**
     * 唯一性校验
     */
    private void checkUnique(${classNameUpper}DTO dto) {
        LambdaQueryWrapper<${classNameUpper}Entity> wrapper = Wrappers.lambdaQuery(${classNameUpper}Entity.class);

        wrapper.ne(Objects.nonNull(dto.getId()), ${classNameUpper}Entity::getId, dto.getId());
        <#list fieldList as field>
            <#if field.uniqueField == 1>
        wrapper.eq(${classNameUpper}Entity::get${field.attrName?cap_first}, dto.get${field.attrName?cap_first}());
            </#if>
        </#list>

        boolean exists = ${className}Mapper.exists(wrapper);
        if (exists) {
            throw new BusinessException("${tableComment}已存在");
        }
    }

    /**
     * 校验能否被删除
     */
    private void checkReference(List<${classNameUpper}Entity> dbEntityList) {
        //TODO 校验是否被引用
        for (${classNameUpper}Entity dbEntity : dbEntityList) {

        }
    }

    private LambdaQueryWrapper<${classNameUpper}Entity> buildQueryWrapper(${classNameUpper}EntityQuery query) {
        LambdaQueryWrapper<${classNameUpper}Entity> wrapper = Wrappers.lambdaQuery(${classNameUpper}Entity.class);
        <#if queryList?has_content>

        //过滤字段
        <#list queryList as field>
            <#if field.queryType == '='>
        wrapper.eq(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == '!='>
        wrapper.ne(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == '>'>
        wrapper.gt(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == '>='>
        wrapper.ge(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == '<'>
        wrapper.lt(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == '<='>
        wrapper.le(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == 'like'>
        wrapper.like(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == 'left like'>
        wrapper.likeLeft(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#elseif field.queryType == 'right like'>
        wrapper.likeRight(MybatisUtil.isNotEmpty(query.get${field.attrName?cap_first}()), ${classNameUpper}Entity::get${field.attrName?cap_first}, query.get${field.attrName?cap_first}());
            <#else>
            </#if>
        </#list>
        </#if>

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}
