package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.domain.query.*;
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
     * 预览数据
     */
    @PostMapping("/preview")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "预览数据")
    public PreviewDataVO preview(@RequestBody CodePreviewQuery codePreviewQuery) throws Exception {
        return generatorService.preview(codePreviewQuery);
    }

    /**
     * 生成代码（下载单个文件代码）
     */
    @GetMapping("/downloadSingle")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "生成代码（下载单个文件代码）")
    public ResponseEntity<byte[]> downloadSingle(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception {
        return generatorService.downloadSingle(singleGeneratorQuery);
    }

    /**
     * 生成代码（本地）
     */
    @PostMapping("/singleLocal")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "生成代码（本地）")
    public void singleLocal(@RequestBody CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception {
        generatorService.singleLocal(singleGeneratorQuery);
    }

    /**
     * 项目生成代码（本地）
     */
    @PostMapping("/project/downloadLocal")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "项目生成代码（本地）")
    public void projectDownloadLocal(@RequestBody GeneratorProjectQuery projectQuery) throws Exception {
        generatorService.projectDownloadLocal(projectQuery);
    }

    /**
     * 项目生成代码（zip压缩包）
     */
    @GetMapping("/project/downloadZip")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "项目生成代码（zip压缩包）")
    public ResponseEntity<byte[]> projectDownloadZip(@ParameterObject GeneratorProjectQuery projectQuery) throws Exception {
        return generatorService.projectDownloadZip(projectQuery);
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
     * 枚举生成代码（本地）
     */
    @PostMapping("/enum/downloadLocal")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "枚举生成代码（本地）")
    public void enumDownloadLocal(@RequestBody GeneratorEnumQuery enumQuery) {
        generatorService.enumDownloadLocal(enumQuery);
    }

    /**
     * 枚举生成代码（zip压缩包）
     */
    @GetMapping("/enum/downloadZip")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "枚举生成代码（zip压缩包）")
    public ResponseEntity<byte[]> enumDownloadZip(@ParameterObject GeneratorEnumQuery enumQuery) {
        return generatorService.enumDownloadZip(enumQuery);
    }

}