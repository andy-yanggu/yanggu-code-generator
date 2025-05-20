package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.EnumDTO;
import com.yanggu.code.generator.domain.query.EnumEntityQuery;
import com.yanggu.code.generator.domain.query.EnumVOQuery;
import com.yanggu.code.generator.domain.vo.EnumGenerateCheckVO;
import com.yanggu.code.generator.domain.vo.EnumVO;
import com.yanggu.code.generator.service.EnumService;
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
 * 枚举管理
 */
@Validated
@RestController
@Tag(name = "枚举管理")
@RequestMapping("/enum")
public class EnumController {

    @Autowired
    private EnumService enumService;

    /**
     * 新增枚举
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增枚举")
    public void add(@RequestBody @Validated(InsertGroup.class) EnumDTO dto) {
        enumService.add(dto);
    }

    /**
     * 修改枚举
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改枚举")
    public void update(@RequestBody @Validated(UpdateGroup.class) EnumDTO dto) {
        enumService.update(dto);
    }

    /**
     * 删除枚举
     *
     * @param id 枚举ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除枚举")
    @Parameter(name = "id", description = "枚举ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "枚举ID不能为空") Long id) {
        enumService.delete(id);
    }

    /**
     * 批量删除枚举
     *
     * @param idList 枚举ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除枚举")
    public void deleteList(@RequestBody @NotEmpty(message = "枚举ID列表不能为空") List<Long> idList) {
        enumService.deleteList(idList);
    }

    /**
     * 枚举详情
     *
     * @param id 枚举ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "枚举详情")
    @Parameter(name = "id", description = "枚举ID", required = true)
    public EnumVO detail(@RequestParam("id") @NotNull(message = "枚举ID不能为空") Long id) {
        return enumService.detail(id);
    }

    /**
     * 枚举批量查询
     *
     * @param idList 枚举ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "枚举批量查询")
    public List<EnumVO> detailList(@RequestBody @NotEmpty(message = "枚举ID列表不能为空") List<Long> idList) {
        return enumService.detailList(idList);
    }

    /**
     * 枚举简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "枚举简单分页")
    public PageVO<EnumVO> entityPage(@RequestBody EnumEntityQuery query) {
        return enumService.entityPage(query);
    }

    /**
     * 枚举简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "枚举简单列表")
    public List<EnumVO> entityList(@RequestBody EnumEntityQuery query) {
        return enumService.entityList(query);
    }

    /**
     * 枚举复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "枚举复杂分页")
    public PageVO<EnumVO> voPage(@RequestBody EnumVOQuery query) {
        return enumService.voPage(query);
    }

    /**
     * 枚举复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "枚举复杂列表")
    public List<EnumVO> voList(@RequestBody EnumVOQuery query) {
        return enumService.voList(query);
    }

    /**
     * 枚举批量生成代码检测接口
     */
    @PostMapping("/generateCheck")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "枚举批量生成代码检测接口")
    public EnumGenerateCheckVO generateCheck(@RequestBody @NotEmpty(message = "枚举ID列表不能为空") List<Long> idList) {
        return enumService.generateCheck(idList);
    }

}