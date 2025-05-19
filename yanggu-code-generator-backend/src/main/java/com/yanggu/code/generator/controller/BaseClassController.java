package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.BaseClassDTO;
import com.yanggu.code.generator.domain.query.BaseClassEntityQuery;
import com.yanggu.code.generator.domain.query.BaseClassVOQuery;
import com.yanggu.code.generator.domain.vo.BaseClassVO;
import com.yanggu.code.generator.service.BaseClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基类管理
 */
@Validated
@RestController
@Tag(name = "基类管理")
@RequestMapping("/baseClass")
public class BaseClassController {

    @Autowired
    private BaseClassService baseClassService;

    /**
     * 新增基类
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增基类")
    public void add(@RequestBody @Validated(InsertGroup.class) BaseClassDTO dto) {
        baseClassService.add(dto);
    }

    /**
     * 修改基类
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改基类")
    public void update(@RequestBody @Validated(UpdateGroup.class) BaseClassDTO dto) {
        baseClassService.update(dto);
    }

    /**
     * 删除基类
     *
     * @param id 基类ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除基类")
    @Parameter(name = "id", description = "基类ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "基类ID不能为空") Long id) {
        baseClassService.delete(id);
    }

    /**
     * 批量删除基类
     *
     * @param idList 基类ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除基类")
    public void deleteList(@RequestBody @NotEmpty(message = "基类ID列表不能为空") List<Long> idList) {
        baseClassService.deleteList(idList);
    }

    /**
     * 基类详情
     *
     * @param id 基类ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "基类详情")
    @Parameter(name = "id", description = "基类ID", required = true)
    public BaseClassVO detail(@RequestParam("id") @NotNull(message = "基类ID不能为空") Long id) {
        return baseClassService.detail(id);
    }

    /**
     * 基类批量查询
     *
     * @param idList 基类ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "基类批量查询")
    public List<BaseClassVO> detailList(@RequestBody @NotEmpty(message = "基类ID列表不能为空") List<Long> idList) {
        return baseClassService.detailList(idList);
    }

    /**
     * 基类简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "基类简单分页")
    public PageVO<BaseClassVO> entityPage(@RequestBody BaseClassEntityQuery query) {
        return baseClassService.entityPage(query);
    }

    /**
     * 基类简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "基类简单列表")
    public List<BaseClassVO> entityList(@RequestBody BaseClassEntityQuery query) {
        return baseClassService.entityList(query);
    }

    /**
     * 基类复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "基类复杂分页")
    public PageVO<BaseClassVO> voPage(@RequestBody BaseClassVOQuery query) {
        return baseClassService.voPage(query);
    }

    /**
     * 基类复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "基类复杂列表")
    public List<BaseClassVO> voList(@RequestBody BaseClassVOQuery query) {
        return baseClassService.voList(query);
    }

}