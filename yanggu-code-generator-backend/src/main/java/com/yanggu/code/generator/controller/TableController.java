package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.TableDTO;
import com.yanggu.code.generator.domain.dto.TableImportDTO;
import com.yanggu.code.generator.domain.query.TableEntityQuery;
import com.yanggu.code.generator.domain.query.TableVOQuery;
import com.yanggu.code.generator.domain.vo.TableGenerateCheckVO;
import com.yanggu.code.generator.domain.vo.TableVO;
import com.yanggu.code.generator.service.TableService;
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
 * 表管理
 */
@Validated
@RestController
@Tag(name = "表管理")
@RequestMapping("/table")
public class TableController {

    @Autowired
    private TableService tableService;

    /**
     * 新增表
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增表")
    public void add(@RequestBody @Validated(InsertGroup.class) TableDTO dto) {
        tableService.add(dto);
    }

    /**
     * 修改表
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改表")
    public void update(@RequestBody @Validated(UpdateGroup.class) TableDTO dto) {
        tableService.update(dto);
    }

    /**
     * 删除表
     *
     * @param id 表ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除表")
    @Parameter(name = "id", description = "表ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "表ID不能为空") Long id) {
        tableService.delete(id);
    }

    /**
     * 批量删除表
     *
     * @param idList 表ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除表")
    public void deleteList(@RequestBody @NotEmpty(message = "表ID列表不能为空") List<Long> idList) {
        tableService.deleteList(idList);
    }

    /**
     * 表详情
     *
     * @param id 表ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "表详情")
    @Parameter(name = "id", description = "表ID", required = true)
    public TableVO detail(@RequestParam("id") @NotNull(message = "表ID不能为空") Long id) {
        return tableService.detail(id);
    }

    /**
     * 表批量查询
     *
     * @param idList 表ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "表批量查询")
    public List<TableVO> detailList(@RequestBody @NotEmpty(message = "表ID列表不能为空") List<Long> idList) {
        return tableService.detailList(idList);
    }

    /**
     * 表简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "表简单分页")
    public PageVO<TableVO> entityPage(@RequestBody TableEntityQuery query) {
        return tableService.entityPage(query);
    }

    /**
     * 表简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "表简单列表")
    public List<TableVO> entityList(@RequestBody TableEntityQuery query) {
        return tableService.entityList(query);
    }

    /**
     * 表复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "表复杂分页")
    public PageVO<TableVO> voPage(@RequestBody TableVOQuery query) {
        return tableService.voPage(query);
    }

    /**
     * 表复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "表复杂列表")
    public List<TableVO> voList(@RequestBody TableVOQuery query) {
        return tableService.voList(query);
    }

    /**
     * 导入表
     */
    @PostMapping("/import")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "导入表")
    public void importTable(@RequestBody @Validated TableImportDTO importDTO) throws Exception {
        tableService.importTable(importDTO);
    }

    /**
     * 同步表
     */
    @PutMapping("/sync")
    @ApiOperationSupport(order = 12)
    @Operation(summary = "同步表")
    public void syncTable(@RequestParam("id") Long id) throws Exception {
        tableService.syncTable(id);
    }

    /**
     * 表批量生成代码检测接口
     */
    @PostMapping("/generateCheck")
    @ApiOperationSupport(order = 13)
    @Operation(summary = "表批量生成代码检测接口")
    public TableGenerateCheckVO generateCheck(@RequestBody @NotEmpty(message = "表ID列表不能为空") List<Long> idList) {
        return tableService.generateCheck(idList);
    }

}