package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.TemplateContentDTO;
import com.yanggu.code.generator.domain.dto.TemplateDTO;
import com.yanggu.code.generator.domain.dto.TemplateDragDTO;
import com.yanggu.code.generator.domain.query.TemplateEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板管理
 */
@Validated
@RestController
@Tag(name = "模板管理")
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    /**
     * 新增模板
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增模板")
    public void add(@RequestBody @Validated(InsertGroup.class) TemplateDTO dto) {
        templateService.add(dto);
    }

    /**
     * 修改模板
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改模板")
    public void update(@RequestBody @Validated(UpdateGroup.class) TemplateDTO dto) {
        templateService.update(dto);
    }

    /**
     * 删除模板
     *
     * @param id 模板ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除模板")
    @Parameter(name = "id", description = "模板ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "模板ID不能为空") Long id) {
        templateService.delete(id);
    }

    /**
     * 批量删除模板
     *
     * @param idList 模板ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除模板")
    public void deleteList(@RequestBody @NotEmpty(message = "模板ID列表不能为空") List<Long> idList) {
        templateService.deleteList(idList);
    }

    /**
     * 模板详情
     *
     * @param id 模板ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "模板详情")
    @Parameter(name = "id", description = "模板ID", required = true)
    public TemplateVO detail(@RequestParam("id") @NotNull(message = "模板ID不能为空") Long id) {
        return templateService.detail(id);
    }

    /**
     * 模板批量查询
     *
     * @param idList 模板ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "模板批量查询")
    public List<TemplateVO> detailList(@RequestBody @NotEmpty(message = "模板ID列表不能为空") List<Long> idList) {
        return templateService.detailList(idList);
    }

    /**
     * 模板简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "模板简单分页")
    public PageVO<TemplateVO> entityPage(@RequestBody TemplateEntityQuery query) {
        return templateService.entityPage(query);
    }

    /**
     * 模板简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "模板简单列表")
    public List<TemplateVO> entityList(@RequestBody TemplateEntityQuery query) {
        return templateService.entityList(query);
    }

    /**
     * 模板复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "模板复杂分页")
    public PageVO<TemplateVO> voPage(@RequestBody TemplateVOQuery query) {
        return templateService.voPage(query);
    }

    /**
     * 模板复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "模板复杂列表")
    public List<TemplateVO> voList(@RequestBody TemplateVOQuery query) {
        return templateService.voList(query);
    }

    /**
     * 模板树数据
     */
    @GetMapping("/tree")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "模板树数据")
    @Parameter(name = "templateGroupId", description = "模板组ID", required = true)
    public List<TemplateVO> tree(@RequestParam("templateGroupId") @NotNull(message = "模板组ID不能为空") Long templateGroupId) {
        return templateService.tree(templateGroupId);
    }

    /**
     * 修改模板内容
     */
    @PutMapping("/updateContent")
    @ApiOperationSupport(order = 12)
    @Operation(summary = "修改模板内容")
    public void updateContent(@RequestBody @Valid TemplateContentDTO contentDTO) {
        templateService.updateContent(contentDTO);
    }

    /**
     * 修改模板父级
     */
    @PutMapping("/updateParent")
    @ApiOperationSupport(order = 13)
    @Operation(summary = "修改模板父级")
    public void updateParent(@RequestBody TemplateDragDTO dragDTO) {
        templateService.updateParent(dragDTO);
    }

}