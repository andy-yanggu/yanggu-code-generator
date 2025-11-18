package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.validation.group.InsertGroup;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyUpdateOrderDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyUpdateRequiredDTO;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 模板组属性管理
 */
@Validated
@RestController
@Tag(name = "模板组属性管理")
@RequestMapping("/gen/templateGroupProperty")
public class TemplateGroupPropertyController {

    @Autowired
    private TemplateGroupPropertyService templateGroupPropertyService;

    /**
     * 新增模板组属性
     */
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "新增模板组属性")
    public void add(@RequestBody @Validated(InsertGroup.class) TemplateGroupPropertyDTO dto) {
        templateGroupPropertyService.add(dto);
    }

    /**
     * 修改模板组属性
     */
    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "修改模板组属性")
    public void update(@RequestBody @Validated(UpdateGroup.class) TemplateGroupPropertyDTO dto) {
        templateGroupPropertyService.update(dto);
    }

    /**
     * 删除模板组属性
     *
     * @param id 模板组属性ID
     */
    @DeleteMapping("/delete")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "删除模板组属性")
    @Parameter(name = "id", description = "模板组属性ID", required = true)
    public void delete(@RequestParam("id") @NotNull(message = "模板组属性ID不能为空") Long id) {
        templateGroupPropertyService.delete(id);
    }

    /**
     * 批量删除模板组属性
     *
     * @param idList 模板组属性ID列表
     */
    @DeleteMapping("/deleteList")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "批量删除模板组属性")
    public void deleteList(@RequestBody @NotEmpty(message = "模板组属性ID列表不能为空") List<Long> idList) {
        templateGroupPropertyService.deleteList(idList);
    }

    /**
     * 模板组属性详情
     *
     * @param id 模板组属性ID
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "模板组属性详情")
    @Parameter(name = "id", description = "模板组属性ID", required = true)
    public TemplateGroupPropertyVO detail(@RequestParam("id") @NotNull(message = "模板组属性ID不能为空") Long id) {
        return templateGroupPropertyService.detail(id);
    }

    /**
     * 模板组属性详情列表
     *
     * @param idList 模板组属性ID列表
     */
    @PostMapping("/detailList")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "模板组属性详情列表")
    public List<TemplateGroupPropertyVO> detailList(@RequestBody @NotEmpty(message = "模板组属性ID列表不能为空") List<Long> idList) {
        return templateGroupPropertyService.detailList(idList);
    }

    /**
     * 模板组属性简单分页
     */
    @PostMapping("/entityPage")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "模板组属性简单分页")
    public PageVO<TemplateGroupPropertyVO> entityPage(@RequestBody TemplateGroupPropertyEntityQuery query) {
        return templateGroupPropertyService.entityPage(query);
    }

    /**
     * 模板组属性简单列表
     */
    @PostMapping("/entityList")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "模板组属性简单列表")
    public List<TemplateGroupPropertyVO> entityList(@RequestBody TemplateGroupPropertyEntityQuery query) {
        return templateGroupPropertyService.entityList(query);
    }

    /**
     * 模板组属性复杂分页
     */
    @PostMapping("/voPage")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "模板组属性复杂分页")
    public PageVO<TemplateGroupPropertyVO> voPage(@RequestBody TemplateGroupPropertyVOQuery query) {
        return templateGroupPropertyService.voPage(query);
    }

    /**
     * 模板组属性复杂列表
     */
    @PostMapping("/voList")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "模板组属性复杂列表")
    public List<TemplateGroupPropertyVO> voList(@RequestBody TemplateGroupPropertyVOQuery query) {
        return templateGroupPropertyService.voList(query);
    }

    /**
     * 导出模板组属性
     *
     * @param idList 模板组属性ID列表
     */
    @GetMapping("/export")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "导出模板组属性")
    @Parameter(name = "idList", description = "模板组属性ID列表", required = true)
    public ResponseEntity<byte[]> export(@RequestParam("idList") @NotEmpty(message = "模板组属性ID列表不能为空") List<Long> idList) {
        return templateGroupPropertyService.export(idList);
    }

    /**
     * 导入模板组属性
     *
     * @param file            文件
     * @param templateGroupId 模板组ID
     */
    @PostMapping("/import")
    @ApiOperationSupport(order = 12)
    @Operation(summary = "导入模板组属性")
    @Parameters({
            @Parameter(name = "templateGroupId", description = "模板组ID", required = true),
            @Parameter(name = "file", description = "文件", in = ParameterIn.DEFAULT, required = true,
                    schema = @Schema(name = "file", format = "binary"))
    })
    public void importTemplateGroup(@RequestParam("file") MultipartFile file,
                                    @RequestParam("templateGroupId") Long templateGroupId) throws Exception {
        templateGroupPropertyService.importTemplateGroupProperty(file, templateGroupId);
    }

    /**
     * 变更是否必填
     */
    @PutMapping("/changeRequired")
    @ApiOperationSupport(order = 13)
    @Operation(summary = "变更是否必填")
    public void changeRequired(@RequestBody @Validated TemplateGroupPropertyUpdateRequiredDTO dto) {
        templateGroupPropertyService.changeRequired(dto);
    }

    /**
     * 变更顺序
     */
    @PutMapping("/changeOrder")
    @ApiOperationSupport(order = 14)
    @Operation(summary = "变更顺序")
    public void changeOrder(@RequestBody @Validated TemplateGroupPropertyUpdateOrderDTO dto) {
        templateGroupPropertyService.changeOrder(dto);
    }

}
