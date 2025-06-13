package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.DatasourceDTO;
import com.yanggu.code.generator.domain.query.DatasourceEntityQuery;
import com.yanggu.code.generator.domain.query.DatasourceVOQuery;
import com.yanggu.code.generator.domain.vo.DatasourceVO;
import com.yanggu.code.generator.service.DatasourceService;
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
 * 数据源管理
 */
@Validated
@RestController
@Tag(name = "数据源管理")
@RequestMapping("/datasource")
public class DatasourceController {

    @Autowired
    private DatasourceService datasourceService;

    /**
     * 新增数据源
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增数据源")
    public void add(@RequestBody @Validated(InsertGroup.class) DatasourceDTO dto) {
        datasourceService.add(dto);
    }

    /**
     * 修改数据源
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改数据源")
    public void update(@RequestBody @Validated(UpdateGroup.class) DatasourceDTO dto) {
        datasourceService.update(dto);
    }

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除数据源")
    @Parameter(name = "id", description = "数据源ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "数据源ID不能为空") Long id) {
        datasourceService.delete(id);
    }

    /**
     * 批量删除数据源
     *
     * @param idList 数据源ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除数据源")
    public void deleteList(@RequestBody @NotEmpty(message = "数据源ID列表不能为空") List<Long> idList) {
        datasourceService.deleteList(idList);
    }

    /**
     * 数据源详情
     *
     * @param id 数据源ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "数据源详情")
    @Parameter(name = "id", description = "数据源ID", required = true)
    public DatasourceVO detail(@RequestParam("id") @NotNull(message = "数据源ID不能为空") Long id) {
        return datasourceService.detail(id);
    }

    /**
     * 数据源批量查询
     *
     * @param idList 数据源ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "数据源批量查询")
    public List<DatasourceVO> detailList(@RequestBody @NotEmpty(message = "数据源ID列表不能为空") List<Long> idList) {
        return datasourceService.detailList(idList);
    }

    /**
     * 数据源简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "数据源简单分页")
    public PageVO<DatasourceVO> entityPage(@RequestBody DatasourceEntityQuery query) {
        return datasourceService.entityPage(query);
    }

    /**
     * 数据源简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "数据源简单列表")
    public List<DatasourceVO> entityList(@RequestBody DatasourceEntityQuery query) {
        return datasourceService.entityList(query);
    }

    /**
     * 数据源复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "数据源复杂分页")
    public PageVO<DatasourceVO> voPage(@RequestBody DatasourceVOQuery query) {
        return datasourceService.voPage(query);
    }

    /**
     * 数据源复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "数据源复杂列表")
    public List<DatasourceVO> voList(@RequestBody DatasourceVOQuery query) {
        return datasourceService.voList(query);
    }

    /**
     * 测试数据源
     *
     * @param id 数据源ID
     */
    @GetMapping("/test")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "测试数据源")
    @Parameter(name = "id", description = "数据源ID", required = true)
    public void test(@RequestParam("id") Long id) throws Exception {
        datasourceService.test(id);
    }

}