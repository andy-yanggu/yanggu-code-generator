package ${projectPackage}.${projectNameDot}.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import ${projectPackage}.${projectNameDot}.common.domain.vo.PageVO;
import ${projectPackage}.${projectNameDot}.common.validation.group.InsertGroup;
import ${projectPackage}.${projectNameDot}.common.validation.group.UpdateGroup;
import ${projectPackage}.${projectNameDot}.service.${classNameUpper}Service;
import ${projectPackage}.${projectNameDot}.domain.dto.${classNameUpper}DTO;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}EntityQuery;
import ${projectPackage}.${projectNameDot}.domain.query.${classNameUpper}VOQuery;
import ${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * ${tableComment}管理
 */
@Validated
@RestController
@Tag(name = "${tableComment}管理")
@RequestMapping("/${functionName}")
public class ${classNameUpper}Controller {

    @Autowired
    private ${classNameUpper}Service ${className}Service;

    /**
     * 新增${tableComment}
     */
    @PostMapping("/add")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 1)
    @Operation(summary = "新增${tableComment}")
    public void add(@RequestBody @Validated(InsertGroup.class) ${classNameUpper}DTO dto) {
        ${className}Service.add(dto);
    }

    /**
     * 修改${tableComment}
     */
    @PutMapping("/update")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 2)
    @Operation(summary = "修改${tableComment}")
    public void update(@RequestBody @Validated(UpdateGroup.class) ${classNameUpper}DTO dto) {
        ${className}Service.update(dto);
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
     * 删除${tableComment}
     *
     * @param id ${tableComment}ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 3)
    @Operation(summary = "删除${tableComment}")
    @Parameter(name = "id", description = "${tableComment}ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "${tableComment}ID不能为空") ${primaryKeyType} id) {
        ${className}Service.delete(id);
    }

    /**
     * 批量删除${tableComment}
     *
     * @param idList ${tableComment}ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 4)
    @Operation(summary = "批量删除${tableComment}")
    public void deleteList(@RequestBody @NotEmpty(message = "${tableComment}ID列表不能为空") List<${primaryKeyType}> idList) {
        ${className}Service.deleteList(idList);
    }

    /**
     * ${tableComment}详情
     *
     * @param id ${tableComment}ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 5)
    @Operation(summary = "${tableComment}详情")
    @Parameter(name = "id", description = "${tableComment}ID", required = true)
    public ${classNameUpper}VO detail(@RequestParam("id") @NotNull(message = "${tableComment}ID不能为空") ${primaryKeyType} id) {
        return ${className}Service.detail(id);
    }

    /**
     * ${tableComment}详情列表
     *
     * @param idList ${tableComment}ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 6)
    @Operation(summary = "${tableComment}详情列表")
    public List<${classNameUpper}VO> detailList(@RequestBody @NotEmpty(message = "${tableComment}ID列表不能为空") List<${primaryKeyType}> idList) {
        return ${className}Service.detailList(idList);
    }

    /**
     * ${tableComment}简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 7)
    @Operation(summary = "${tableComment}简单分页")
    public PageVO<${classNameUpper}VO> entityPage(@RequestBody ${classNameUpper}EntityQuery query) {
        return ${className}Service.entityPage(query);
    }

    /**
     * ${tableComment}简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 8)
    @Operation(summary = "${tableComment}简单列表")
    public List<${classNameUpper}VO> entityList(@RequestBody ${classNameUpper}EntityQuery query) {
        return ${className}Service.entityList(query);
    }

    /**
     * ${tableComment}复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 9)
    @Operation(summary = "${tableComment}复杂分页")
    public PageVO<${classNameUpper}VO> voPage(@RequestBody ${classNameUpper}VOQuery query) {
        return ${className}Service.voPage(query);
    }

    /**
     * ${tableComment}复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(<#if author!?length gt 0>author = "${author}", </#if>order = 10)
    @Operation(summary = "${tableComment}复杂列表")
    public List<${classNameUpper}VO> voList(@RequestBody ${classNameUpper}VOQuery query) {
        return ${className}Service.voList(query);
    }

}