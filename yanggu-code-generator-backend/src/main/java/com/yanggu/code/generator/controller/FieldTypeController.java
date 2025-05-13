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
import com.yanggu.code.generator.service.FieldTypeService;
import com.yanggu.code.generator.domain.dto.FieldTypeDTO;
import com.yanggu.code.generator.domain.query.FieldTypeEntityQuery;
import com.yanggu.code.generator.domain.query.FieldTypeVOQuery;
import com.yanggu.code.generator.domain.vo.FieldTypeVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 字段类型管理
 */
@Validated
@RestController
@Tag(name = "字段类型管理")
@RequestMapping("/fieldType")
public class FieldTypeController {

    @Autowired
    private FieldTypeService fieldTypeService;

    /**
     * 新增字段类型
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增字段类型")
    public void add(@RequestBody @Validated(InsertGroup.class) FieldTypeDTO dto) {
        fieldTypeService.add(dto);
    }

    /**
     * 修改字段类型
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改字段类型")
    public void update(@RequestBody @Validated(UpdateGroup.class) FieldTypeDTO dto) {
        fieldTypeService.update(dto);
    }

    /**
     * 删除字段类型
     *
     * @param id 字段类型ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除字段类型")
    @Parameter(name = "id", description = "字段类型ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "字段类型ID不能为空") Long id) {
        fieldTypeService.delete(id);
    }

    /**
     * 批量删除字段类型
     *
     * @param idList 字段类型ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除字段类型")
    public void deleteList(@RequestBody @NotEmpty(message = "字段类型ID列表不能为空") List<Long> idList) {
        fieldTypeService.deleteList(idList);
    }

    /**
     * 字段类型详情
     *
     * @param id 字段类型ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "字段类型详情")
    @Parameter(name = "id", description = "字段类型ID", required = true)
    public FieldTypeVO detail(@RequestParam("id") @NotNull(message = "字段类型ID不能为空") Long id) {
        return fieldTypeService.detail(id);
    }

    /**
     * 字段类型批量查询
     *
     * @param idList 字段类型ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "字段类型批量查询")
    public List<FieldTypeVO> detailList(@RequestBody @NotEmpty(message = "字段类型ID列表不能为空") List<Long> idList) {
        return fieldTypeService.detailList(idList);
    }

    /**
     * 字段类型简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "字段类型简单分页")
    public PageVO<FieldTypeVO> entityPage(@RequestBody FieldTypeEntityQuery query) {
        return fieldTypeService.entityPage(query);
    }

    /**
     * 字段类型简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "字段类型简单列表")
    public List<FieldTypeVO> entityList(@RequestBody FieldTypeEntityQuery query) {
        return fieldTypeService.entityList(query);
    }

    /**
     * 字段类型复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "字段类型复杂分页")
    public PageVO<FieldTypeVO> voPage(@RequestBody FieldTypeVOQuery query) {
        return fieldTypeService.voPage(query);
    }

    /**
     * 字段类型复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "字段类型复杂列表")
    public List<FieldTypeVO> voList(@RequestBody FieldTypeVOQuery query) {
        return fieldTypeService.voList(query);
    }

    /**
     * 去重字段类型列表
     */
    @GetMapping("/distinctList")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "去重字段类型列表")
    public List<String> distinctList() {
        return fieldTypeService.distinctList();
    }

}