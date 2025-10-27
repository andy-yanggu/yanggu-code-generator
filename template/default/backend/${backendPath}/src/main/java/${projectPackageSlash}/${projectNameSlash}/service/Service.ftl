package ${projectPackage}.${projectNameDot}.service;

import ${projectPackage}.${projectNameDot}.common.domain.vo.PageVO;
import ${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO;
import ${projectPackage}.${projectNameDot}.domain.dto.${classNameUpper}DTO;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}EntityQuery;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}VOQuery;
import ${projectPackage}.${projectNameDot}.domain.entity.${classNameUpper}Entity;
import com.baomidou.mybatisplus.extension.service.IService;
<#if hasFunction(generatorFunction, 6)>
import org.springframework.web.multipart.MultipartFile;
</#if>
<#if hasFunction(generatorFunction, 7)>
import org.springframework.http.ResponseEntity;
</#if>

import java.util.List;

/**
 * ${tableComment}服务层
 */
public interface ${classNameUpper}Service extends IService<${classNameUpper}Entity> {

<#-- 判断某个功能编号是否在 generatorFunction 列表中 -->
<#function hasFunction generatorFunction target>
    <#if generatorFunction?? && (generatorFunction?seq_contains(target))>
        <#return true>
    </#if>
    <#return false>
</#function>
<#if hasFunction(generatorFunction, 0)>
    /**
     * 新增
     */
    ${classNameUpper}Entity add(${classNameUpper}DTO dto);

</#if>
<#if hasFunction(generatorFunction, 1)>
    /**
     * 修改
     */
    void update(${classNameUpper}DTO dto);

</#if>
<#function getPrimaryKeyType fieldList>
    <#list fieldList as field>
        <#if field.primaryPk == 1>
            <#return field.attrType>
        </#if>
    </#list>
    <#return Long>
</#function>
<#assign primaryKeyType = getPrimaryKeyType(fieldList)>
<#if hasFunction(generatorFunction, 2)>
    /**
     * 删除
     */
    void delete(${primaryKeyType} id);

    /**
     * 批量删除
     */
    void deleteList(List<${primaryKeyType}> idList);

</#if>
<#if hasFunction(generatorFunction, 3)>
    /**
     * 详情
     */
    ${classNameUpper}VO detail(${primaryKeyType} id);

    /**
     * 详情列表
     */
    List<${classNameUpper}VO> detailList(List<${primaryKeyType}> idList);

</#if>
<#if hasFunction(generatorFunction, 4)>
    /**
     * 简单分页
     */
    PageVO<${classNameUpper}VO> entityPage(${classNameUpper}EntityQuery query);

</#if>
<#if hasFunction(generatorFunction, 5)>
    /**
     * 简单列表
     */
    List<${classNameUpper}VO> entityList(${classNameUpper}EntityQuery query);

</#if>
<#if hasFunction(generatorFunction, 4)>
    /**
     * 复杂分页
     */
    PageVO<${classNameUpper}VO> voPage(${classNameUpper}VOQuery query);

</#if>
<#if hasFunction(generatorFunction, 5)>
    /**
     * 复杂列表
     */
    List<${classNameUpper}VO> voList(${classNameUpper}VOQuery query);

</#if>
<#if hasFunction(generatorFunction, 6)>
    /**
     * 导入
     */
    void import(MultipartFile file) throws Exception;

</#if>
<#if hasFunction(generatorFunction, 7)>
    /**
     * 导出
     */
    ResponseEntity<byte[]> export(List<${primaryKeyType}> idList);

</#if>
}