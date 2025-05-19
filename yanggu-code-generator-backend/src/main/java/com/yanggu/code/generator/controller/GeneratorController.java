package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.domain.query.GeneratorProjectQuery;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewDataVO;
import com.yanggu.code.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
     * 表预览数据
     */
    @GetMapping("/table/preview")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "表预览数据")
    public PreviewDataVO tablePreview(@RequestParam("tableId") Long tableId) {
        return generatorService.tablePreview(tableId);
    }

    /**
     * 表生成代码（下载单个文件代码）
     */
    @GetMapping("/table/downloadSingle")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "表生成代码（下载单个文件代码）")
    public ResponseEntity<byte[]> tableDownloadSingle(@RequestParam("tableId") Long tableId,
                                                      @RequestParam("templateId") Long templateId) throws Exception {
        return generatorService.tableDownloadSingle(tableId, templateId);
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
     * 项目预览数据
     */
    @GetMapping("/project/preview")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "项目预览数据")
    public PreviewDataVO projectPreview(@RequestParam("projectId") Long projectId) throws Exception {
        return generatorService.projectPreview(projectId);
    }

    /**
     * 项目生成代码（本地）
     */
    @PostMapping("/project/downloadLocal")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "项目生成代码（本地）")
    public void projectDownloadLocal(@RequestBody GeneratorProjectQuery projectQuery) throws Exception {
        generatorService.projectDownloadLocal(projectQuery);
    }

    /**
     * 项目生成代码（zip压缩包）
     */
    @GetMapping("/project/downloadZip")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "项目生成代码（zip压缩包）")
    public ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception {
        return generatorService.projectDownloadZip(projectQuery);
    }

    /**
     * 项目生成代码（下载单个文件代码）
     */
    @GetMapping("/project/downloadSingle")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "项目生成代码（下载单个文件代码）")
    public ResponseEntity<byte[]> projectDownloadSingle(@RequestParam("templateGroupType") Integer templateGroupType,
                                                        @RequestParam("id") Long id,
                                                        @RequestParam("templateId") Long templateId) throws Exception {
        return generatorService.projectDownloadSingle(templateGroupType, id, templateId);
    }

    /**
     * 枚举预览数据
     */
    @GetMapping("/enum/preview")
    @ApiOperationSupport(order = 12)
    @Operation(summary = "枚举预览数据")
    public PreviewDataVO enumPreview(@RequestParam("enumId") Long enumId) throws Exception {
        return generatorService.enumPreview(enumId);
    }

    ///**
    // * 项目生成代码（本地）
    // */
    //@PostMapping("/project/downloadLocal")
    //@ApiOperationSupport(order = 9)
    //@Operation(summary = "项目生成代码（本地）")
    //public void projectDownloadLocal(@RequestBody GeneratorProjectQuery projectQuery) throws Exception {
    //    generatorService.projectDownloadLocal(projectQuery);
    //}
    //
    ///**
    // * 项目生成代码（zip压缩包）
    // */
    //@GetMapping("/project/downloadZip")
    //@ApiOperationSupport(order = 10)
    //@Operation(summary = "项目生成代码（zip压缩包）")
    //public ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception {
    //    return generatorService.projectDownloadZip(projectQuery);
    //}
    //
    ///**
    // * 项目生成代码（下载单个文件代码）
    // */
    //@GetMapping("/project/downloadSingle")
    //@ApiOperationSupport(order = 11)
    //@Operation(summary = "项目生成代码（下载单个文件代码）")
    //public ResponseEntity<byte[]> projectDownloadSingle(@RequestParam("templateGroupType") Integer templateGroupType,
    //                                                    @RequestParam("id") Long id,
    //                                                    @RequestParam("templateId") Long templateId) throws Exception {
    //    return generatorService.projectDownloadSingle(templateGroupType, id, templateId);
    //}

}