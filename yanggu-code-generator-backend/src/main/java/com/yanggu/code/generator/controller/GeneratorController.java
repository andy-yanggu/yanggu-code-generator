package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewVO;
import com.yanggu.code.generator.domain.vo.TreeVO;
import com.yanggu.code.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
     * 表预览代码
     */
    @PostMapping("/table/preview")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "表预览代码")
    public List<PreviewVO> tablePreview(@RequestBody GeneratorTableQuery tableQuery) {
        return generatorService.tablePreview(tableQuery);
    }

    /**
     * 表树形数据
     */
    @PostMapping("/table/treeData")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "表树形数据")
    public List<TreeVO> tableTreeData(@RequestBody GeneratorTableQuery tableQuery) {
        return generatorService.tableTreeData(tableQuery);
    }

    /**
     * 表批量生成代码（zip压缩包）
     */
    @GetMapping("/table/batchDownloadZip")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "表批量生成代码（zip压缩包）")
    public ResponseEntity<byte[]> tableDownloadZip(@RequestParam("tableIdList") List<Long> tableIdList) throws Exception {
        return generatorService.tableBatchDownloadZip(tableIdList);
    }

    /**
     * 表生成代码（下载单个文件代码）
     */
    @GetMapping("/table/download-template-content")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "表生成代码（下载单个文件代码）")
    public ResponseEntity<byte[]> tableDownloadTemplateContent(@RequestParam("tableId") Long tableId,
                                                               @RequestParam("templateId") Long templateId) throws Exception {
        return generatorService.tableDownloadTemplateContent(tableId, templateId);
    }

    /**
     * 表生成代码（本地）
     */
    @PostMapping("/table/downloadLocal")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "表生成代码（本地）")
    public void tableDownloadLocal(@RequestBody GeneratorTableQuery tableQuery) {
        generatorService.tableDownloadLocal(tableQuery);
    }

    /**
     * 表生成代码（zip压缩包）
     */
    @GetMapping("/table/downloadZip")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "表生成代码（zip压缩包）")
    public ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) throws IOException {
        return generatorService.tableDownloadZip(tableQuery);
    }

    /**
     * 项目预览代码
     */
    @GetMapping("/project/preview")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "项目预览代码")
    public List<PreviewVO> projectPreview(@RequestParam("projectId") Long projectId) throws Exception {
        return generatorService.buildProjectPreviewList(projectId);
    }

    /**
     * 项目树形数据
     */
    @GetMapping("/project/treeData")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "项目树形数据")
    public List<TreeVO> projectTreeData(@RequestParam("projectId") Long projectId) throws Exception {
        return generatorService.treeData(projectId);
    }

}