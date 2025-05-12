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
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.domain.dto.ProjectDTO;
import com.yanggu.code.generator.domain.query.ProjectEntityQuery;
import com.yanggu.code.generator.domain.query.ProjectVOQuery;
import com.yanggu.code.generator.domain.vo.ProjectVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 项目管理
 */
@Validated
@RestController
@Tag(name = "项目管理")
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 新增项目
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增项目")
    public void add(@RequestBody @Validated(InsertGroup.class) ProjectDTO dto) {
        projectService.add(dto);
    }

    /**
     * 修改项目
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改项目")
    public void update(@RequestBody @Validated(UpdateGroup.class) ProjectDTO dto) {
        projectService.update(dto);
    }

    /**
     * 删除项目
     *
     * @param id 项目ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除项目")
    @Parameter(name = "id", description = "项目ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "项目ID不能为空") Long id) {
        projectService.delete(id);
    }

    /**
     * 批量删除项目
     *
     * @param idList 项目ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除项目")
    public void deleteList(@RequestBody @NotEmpty(message = "项目ID列表不能为空") List<Long> idList) {
        projectService.deleteList(idList);
    }

    /**
     * 项目详情
     *
     * @param id 项目ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "项目详情")
    @Parameter(name = "id", description = "项目ID", required = true)
    public ProjectVO detail(@RequestParam("id") @NotNull(message = "项目ID不能为空") Long id) {
        return projectService.detail(id);
    }

    /**
     * 项目批量查询
     *
     * @param idList 项目ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "项目批量查询")
    public List<ProjectVO> detailList(@RequestBody @NotEmpty(message = "项目ID列表不能为空") List<Long> idList) {
        return projectService.detailList(idList);
    }

    /**
     * 项目简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "项目简单分页")
    public PageVO<ProjectVO> entityPage(@RequestBody ProjectEntityQuery query) {
        return projectService.entityPage(query);
    }

    /**
     * 项目简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "项目简单列表")
    public List<ProjectVO> entityList(@RequestBody ProjectEntityQuery query) {
        return projectService.entityList(query);
    }

    /**
     * 项目复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "项目复杂分页")
    public PageVO<ProjectVO> voPage(@RequestBody ProjectVOQuery query) {
        return projectService.voPage(query);
    }

    /**
     * 项目复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "项目复杂列表")
    public List<ProjectVO> voList(@RequestBody ProjectVOQuery query) {
        return projectService.voList(query);
    }

}