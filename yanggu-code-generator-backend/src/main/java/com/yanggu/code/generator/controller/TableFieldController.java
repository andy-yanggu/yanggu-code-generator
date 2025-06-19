package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.query.TableFieldVOQuery;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import com.yanggu.code.generator.service.TableFieldService;
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
 * 表字段管理
 */
@Validated
@RestController
@Tag(name = "表字段管理")
@RequestMapping("/tableField")
public class TableFieldController {

    @Autowired
    private TableFieldService tableFieldService;

    /**
     * 表字段简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "表字段简单列表")
    public List<TableFieldVO> entityList(@RequestBody TableFieldEntityQuery query) {
        return tableFieldService.entityList(query);
    }

    /**
     * 表字段提交
     *
     * @param submitList 表字段提交列表
     */
    @PostMapping("/submitList")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "表字段提交")
    public void submitList(@RequestBody @NotEmpty(message = "表字段提交列表") List<TableFieldDTO> submitList) {
        tableFieldService.submitList(submitList);
    }

}