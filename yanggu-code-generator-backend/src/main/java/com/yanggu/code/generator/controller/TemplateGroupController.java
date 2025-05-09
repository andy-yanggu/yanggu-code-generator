package com.yanggu.code.generator.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.service.TemplateGroupService;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.query.TemplateGroupEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 模板组管理
 */
@Validated
@RestController
@Tag(name = "模板组管理")
@RequestMapping("/TemplateGroup")
public class TemplateGroupController {

    @Autowired
    private TemplateGroupService templateGroupService;

    /**
     * 新增模板组
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增模板组")
    public void add(@RequestBody @Validated(InsertGroup.class) TemplateGroupDTO dto) {
        templateGroupService.add(dto);
    }

    /**
     * 修改模板组
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改模板组")
    public void update(@RequestBody @Validated(UpdateGroup.class) TemplateGroupDTO dto) {
        templateGroupService.update(dto);
    }

    /**
     * 删除模板组
     *
     * @param id 模板组ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除模板组")
    @Parameter(name = "id", description = "模板组ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "模板组ID不能为空") Long id) {
        templateGroupService.delete(id);
    }

    /**
     * 批量删除模板组
     *
     * @param idList 模板组ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除模板组")
    public void deleteList(@RequestBody @NotEmpty(message = "模板组ID列表不能为空") List<Long> idList) {
        templateGroupService.deleteList(idList);
    }

    /**
     * 模板组详情
     *
     * @param id 模板组ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "模板组详情")
    @Parameter(name = "id", description = "模板组ID", required = true)
    public TemplateGroupVO detail(@RequestParam("id") @NotNull(message = "模板组ID不能为空") Long id) {
        return templateGroupService.detail(id);
    }

    /**
     * 模板组批量查询
     *
     * @param idList 模板组ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "模板组批量查询")
    public List<TemplateGroupVO> detailList(@RequestBody @NotEmpty(message = "模板组ID列表不能为空") List<Long> idList) {
        return templateGroupService.detailList(idList);
    }

    /**
     * 模板组简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "模板组简单分页")
    public PageVO<TemplateGroupVO> entityPage(@RequestBody TemplateGroupEntityQuery query) {
        return templateGroupService.entityPage(query);
    }

    /**
     * 模板组简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "模板组简单列表")
    public List<TemplateGroupVO> entityList(@RequestBody TemplateGroupEntityQuery query) {
        return templateGroupService.entityList(query);
    }

    /**
     * 模板组复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "模板组复杂分页")
    public PageVO<TemplateGroupVO> voPage(@RequestBody TemplateGroupVOQuery query) {
        return templateGroupService.voPage(query);
    }

    /**
     * 模板组复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "模板组复杂列表")
    public List<TemplateGroupVO> voList(@RequestBody TemplateGroupVOQuery query) {
        return templateGroupService.voList(query);
    }

}