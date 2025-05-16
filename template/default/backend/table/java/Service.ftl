package ${projectPackage}.${projectNameDot}.service;

import ${projectPackage}.${projectNameDot}.common.domain.vo.PageVO;
import ${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO;
import ${projectPackage}.${projectNameDot}.domain.dto.${classNameUpper}DTO;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}EntityQuery;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}VOQuery;
import ${projectPackage}.${projectNameDot}.domain.entity.${classNameUpper}Entity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * ${tableComment}服务层
 */
public interface ${classNameUpper}Service extends IService<${classNameUpper}Entity> {

    /**
     * 新增
     */
    void add(${classNameUpper}DTO dto);

    /**
     * 修改
     */
    void update(${classNameUpper}DTO dto);
<#function getPrimaryKeyType fieldList>
    <#list fieldList as field>
        <#if field.primaryPk>
            <#return field.attrType>
        </#if>
    </#list>
    <#return Long>
</#function>
<#assign primaryKeyType = getPrimaryKeyType(fieldList)>

    /**
     * 删除
     */
    void delete(${primaryKeyType} id);

    /**
     * 批量删除
     */
    void deleteList(List<${primaryKeyType}> idList);

    /**
     * 详情
     */
    ${classNameUpper}VO detail(${primaryKeyType} id);

    /**
     * 批量查询
     */
    List<${classNameUpper}VO> detailList(List<${primaryKeyType}> idList);

    /**
     * 简单分页
     */
    PageVO<${classNameUpper}VO> entityPage(${classNameUpper}EntityQuery query);

    /**
     * 简单列表
     */
    List<${classNameUpper}VO> entityList(${classNameUpper}EntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<${classNameUpper}VO> voPage(${classNameUpper}VOQuery query);

    /**
     * 复杂列表
     */
    List<${classNameUpper}VO> voList(${classNameUpper}VOQuery query);

}