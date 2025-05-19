package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.EnumItemDTO;
import com.yanggu.code.generator.domain.query.EnumItemEntityQuery;
import com.yanggu.code.generator.domain.query.EnumItemVOQuery;
import com.yanggu.code.generator.domain.vo.EnumItemVO;
import com.yanggu.code.generator.service.EnumItemService;
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
 * 枚举项管理
 */
@Validated
@RestController
@Tag(name = "枚举项管理")
@RequestMapping("/enumItem")
public class EnumItemController {

    @Autowired
    private EnumItemService enumItemService;

    /**
     * 新增枚举项
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增枚举项")
    public void add(@RequestBody @Validated(InsertGroup.class) EnumItemDTO dto) {
        enumItemService.add(dto);
    }

    /**
     * 修改枚举项
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改枚举项")
    public void update(@RequestBody @Validated(UpdateGroup.class) EnumItemDTO dto) {
        enumItemService.update(dto);
    }

    /**
     * 删除枚举项
     *
     * @param id 枚举项ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除枚举项")
    @Parameter(name = "id", description = "枚举项ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "枚举项ID不能为空") Long id) {
        enumItemService.delete(id);
    }

    /**
     * 批量删除枚举项
     *
     * @param idList 枚举项ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除枚举项")
    public void deleteList(@RequestBody @NotEmpty(message = "枚举项ID列表不能为空") List<Long> idList) {
        enumItemService.deleteList(idList);
    }

    /**
     * 枚举项详情
     *
     * @param id 枚举项ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "枚举项详情")
    @Parameter(name = "id", description = "枚举项ID", required = true)
    public EnumItemVO detail(@RequestParam("id") @NotNull(message = "枚举项ID不能为空") Long id) {
        return enumItemService.detail(id);
    }

    /**
     * 枚举项批量查询
     *
     * @param idList 枚举项ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "枚举项批量查询")
    public List<EnumItemVO> detailList(@RequestBody @NotEmpty(message = "枚举项ID列表不能为空") List<Long> idList) {
        return enumItemService.detailList(idList);
    }

    /**
     * 枚举项简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "枚举项简单分页")
    public PageVO<EnumItemVO> entityPage(@RequestBody EnumItemEntityQuery query) {
        return enumItemService.entityPage(query);
    }

    /**
     * 枚举项简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "枚举项简单列表")
    public List<EnumItemVO> entityList(@RequestBody EnumItemEntityQuery query) {
        return enumItemService.entityList(query);
    }

    /**
     * 枚举项复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "枚举项复杂分页")
    public PageVO<EnumItemVO> voPage(@RequestBody EnumItemVOQuery query) {
        return enumItemService.voPage(query);
    }

    /**
     * 枚举项复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "枚举项复杂列表")
    public List<EnumItemVO> voList(@RequestBody EnumItemVOQuery query) {
        return enumItemService.voList(query);
    }

}