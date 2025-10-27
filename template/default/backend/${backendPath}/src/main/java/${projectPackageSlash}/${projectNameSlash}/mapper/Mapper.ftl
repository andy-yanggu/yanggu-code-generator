package ${projectPackage}.${projectNameDot}.mapper;

import ${projectPackage}.${projectNameDot}.common.mybatis.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import ${projectPackage}.${projectNameDot}.domain.entity.${classNameUpper}Entity;
import ${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}EntityQuery;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}VOQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ${tableComment}Mapper
 */
@Mapper
@Repository
public interface ${classNameUpper}Mapper extends BaseMapperPlus<${classNameUpper}Entity> {

<#-- 判断某个功能编号是否在 generatorFunction 列表中 -->
<#function hasFunction generatorFunction target>
    <#if generatorFunction?? && (generatorFunction?seq_contains(target))>
        <#return true>
    </#if>
    <#return false>
</#function>
<#if hasFunction(generatorFunction, 4)>
    /**
     * Entity分页
     */
    IPage<${classNameUpper}Entity> entityPage(@Param("query") ${classNameUpper}EntityQuery query);

</#if>
<#if hasFunction(generatorFunction, 5)>
    /**
     * Entity列表
     */
    List<${classNameUpper}Entity> entityList(@Param("query") ${classNameUpper}EntityQuery query);

</#if>
<#if hasFunction(generatorFunction, 4)>
    /**
     * VO分页
     */
    IPage<${classNameUpper}VO> voPage(@Param("query") ${classNameUpper}VOQuery query);

</#if>
<#if hasFunction(generatorFunction, 5)>
    /**
     * VO列表
     */
    List<${classNameUpper}VO> voList(@Param("query") ${classNameUpper}VOQuery query);

</#if>
}
