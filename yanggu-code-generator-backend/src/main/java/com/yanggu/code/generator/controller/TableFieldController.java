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
import com.yanggu.code.generator.service.TableFieldService;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.query.TableFieldVOQuery;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 表字段管理
 */
@Validated
@RestController
@Tag(name = "表字段管理")
@RequestMapping("/TableField")
public class TableFieldController {

    @Autowired
    private TableFieldService tableFieldService;

    /**
     * 新增表字段
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增表字段")
    public void add(@RequestBody @Validated(InsertGroup.class) TableFieldDTO dto) {
        tableFieldService.add(dto);
    }

    /**
     * 修改表字段
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改表字段")
    public void update(@RequestBody @Validated(UpdateGroup.class) TableFieldDTO dto) {
        tableFieldService.update(dto);
    }

    /**
     * 删除表字段
     *
     * @param id 表字段ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除表字段")
    @Parameter(name = "id", description = "表字段ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "表字段ID不能为空") Long id) {
        tableFieldService.delete(id);
    }

    /**
     * 批量删除表字段
     *
     * @param idList 表字段ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除表字段")
    public void deleteList(@RequestBody @NotEmpty(message = "表字段ID列表不能为空") List<Long> idList) {
        tableFieldService.deleteList(idList);
    }

    /**
     * 表字段详情
     *
     * @param id 表字段ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "表字段详情")
    @Parameter(name = "id", description = "表字段ID", required = true)
    public TableFieldVO detail(@RequestParam("id") @NotNull(message = "表字段ID不能为空") Long id) {
        return tableFieldService.detail(id);
    }

    /**
     * 表字段批量查询
     *
     * @param idList 表字段ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "表字段批量查询")
    public List<TableFieldVO> detailList(@RequestBody @NotEmpty(message = "表字段ID列表不能为空") List<Long> idList) {
        return tableFieldService.detailList(idList);
    }

    /**
     * 表字段简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "表字段简单分页")
    public PageVO<TableFieldVO> entityPage(@RequestBody TableFieldEntityQuery query) {
        return tableFieldService.entityPage(query);
    }

    /**
     * 表字段简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "表字段简单列表")
    public List<TableFieldVO> entityList(@RequestBody TableFieldEntityQuery query) {
        return tableFieldService.entityList(query);
    }

    /**
     * 表字段复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "表字段复杂分页")
    public PageVO<TableFieldVO> voPage(@RequestBody TableFieldVOQuery query) {
        return tableFieldService.voPage(query);
    }

    /**
     * 表字段复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "表字段复杂列表")
    public List<TableFieldVO> voList(@RequestBody TableFieldVOQuery query) {
        return tableFieldService.voList(query);
    }

}