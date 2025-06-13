package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.domain.query.GeneratorEnumQuery;
import com.yanggu.code.generator.domain.query.GeneratorProjectQuery;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewDataVO;
import com.yanggu.code.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 代码生成管理
 */
@Validated
@RestController
@Tag(name = "代码生成管理")
@RequestMapping("/generator")
public class GeneratorController {

    @Autowired
    private GeneratorService generatorService;

    /**
     * 项目预览数据
     *
     * @param projectId 项目ID
     */
    @GetMapping("/project/preview")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "项目预览数据")
    @Parameter(name = "projectId", description = "项目ID", required = true)
    public PreviewDataVO projectPreview(@RequestParam("projectId") Long projectId) throws Exception {
        return generatorService.projectPreview(projectId);
    }

    /**
     * 项目生成代码（本地）
     */
    @PostMapping("/project/downloadLocal")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "项目生成代码（本地）")
    public void projectDownloadLocal(@RequestBody GeneratorProjectQuery projectQuery) throws Exception {
        generatorService.projectDownloadLocal(projectQuery);
    }

    /**
     * 项目生成代码（zip压缩包）
     */
    @GetMapping("/project/downloadZip")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "项目生成代码（zip压缩包）")
    public ResponseEntity<byte[]> projectDownloadZip(@ParameterObject GeneratorProjectQuery projectQuery) throws Exception {
        return generatorService.projectDownloadZip(projectQuery);
    }

    /**
     * 项目生成代码（下载单个文件代码）
     *
     * @param templateGroupType 模板组类型
     * @param id                项目、表和枚举ID
     * @param templateId        模板ID
     */
    @GetMapping("/project/downloadSingle")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "项目生成代码（下载单个文件代码）")
    @Parameters({
            @Parameter(name = "templateGroupType", description = "模板组类型", required = true),
            @Parameter(name = "id", description = "项目、表和枚举ID", required = true),
            @Parameter(name = "templateId", description = "模板ID", required = true)
    })
    public ResponseEntity<byte[]> projectDownloadSingle(@RequestParam("templateGroupType") Integer templateGroupType,
                                                        @RequestParam("id") Long id,
                                                        @RequestParam("templateId") Long templateId) throws Exception {
        return generatorService.projectDownloadSingle(templateGroupType, id, templateId);
    }

    /**
     * 表预览数据
     *
     * @param tableId 表ID
     */
    @GetMapping("/table/preview")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "表预览数据")
    @Parameter(name = "tableId", description = "表ID", required = true)
    public PreviewDataVO tablePreview(@RequestParam("tableId") Long tableId) {
        return generatorService.tablePreview(tableId);
    }

    /**
     * 表生成代码（本地）
     */
    @PostMapping("/table/downloadLocal")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "表生成代码（本地）")
    public void tableDownloadLocal(@RequestBody GeneratorTableQuery tableQuery) {
        generatorService.tableDownloadLocal(tableQuery);
    }

    /**
     * 表生成代码（zip压缩包）
     */
    @GetMapping("/table/downloadZip")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "表生成代码（zip压缩包）")
    public ResponseEntity<byte[]> tableDownloadZip(@ParameterObject GeneratorTableQuery tableQuery) throws IOException {
        return generatorService.tableDownloadZip(tableQuery);
    }

    /**
     * 表生成代码（下载单个文件代码）
     *
     * @param tableId    表ID
     * @param templateId 模板ID
     */
    @GetMapping("/table/downloadSingle")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "表生成代码（下载单个文件代码）")
    @Parameters({
            @Parameter(name = "tableId", description = "表ID", required = true),
            @Parameter(name = "templateId", description = "模板ID", required = true)
    })
    public ResponseEntity<byte[]> tableDownloadSingle(@RequestParam("tableId") Long tableId,
                                                      @RequestParam("templateId") Long templateId) throws Exception {
        return generatorService.tableDownloadSingle(tableId, templateId);
    }

    /**
     * 枚举预览数据
     *
     * @param enumId 枚举ID
     */
    @GetMapping("/enum/preview")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "枚举预览数据")
    @Parameter(name = "enumId", description = "枚举ID", required = true)
    public PreviewDataVO enumPreview(@RequestParam("enumId") Long enumId) {
        return generatorService.enumPreview(enumId);
    }

    /**
     * 枚举生成代码（本地）
     */
    @PostMapping("/enum/downloadLocal")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "枚举生成代码（本地）")
    public void enumDownloadLocal(@RequestBody GeneratorEnumQuery enumQuery) {
        generatorService.enumDownloadLocal(enumQuery);
    }

    /**
     * 枚举生成代码（zip压缩包）
     */
    @GetMapping("/enum/downloadZip")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "枚举生成代码（zip压缩包）")
    public ResponseEntity<byte[]> enumDownloadZip(@ParameterObject GeneratorEnumQuery enumQuery) {
        return generatorService.enumDownloadZip(enumQuery);
    }

    /**
     * 枚举生成代码（下载单个文件代码）
     *
     * @param enumId     枚举ID
     * @param templateId 模板ID
     */
    @GetMapping("/enum/downloadSingle")
    @ApiOperationSupport(order = 12)
    @Operation(summary = "枚举生成代码（下载单个文件代码）")
    @Parameters({
            @Parameter(name = "enumId", description = "枚举ID", required = true),
            @Parameter(name = "templateId", description = "模板ID", required = true)
    })
    public ResponseEntity<byte[]> enumDownloadSingle(@RequestParam("enumId") Long enumId,
                                                     @RequestParam("templateId") Long templateId) {
        return generatorService.enumDownloadSingle(enumId, templateId);
    }

}