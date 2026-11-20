package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 代码生成管理
 */
@Validated
@RestController
@Tag(name = "代码生成管理")
@RequestMapping("/gen/generator")
public class GeneratorController {

    @Autowired
    private GeneratorService generatorService;

    @PostMapping("/preview")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "预览数据")
    public List<PreviewTemplateVO> preview(@RequestBody GeneratorQuery query) {
        return generatorService.preview(query);
    }

    @PostMapping("/downloadZip")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "生成代码（zip压缩包）")
    public ResponseEntity<byte[]> downloadZip(@RequestBody GeneratorQuery query) {
        return generatorService.downloadZip(query);
    }

    @PostMapping("/downloadLocal")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "生成代码（服务器本地）")
    public void downloadLocal(@RequestBody GeneratorQuery query) {
        generatorService.downloadLocal(query);
    }

    @PostMapping("/downloadSingle")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "生成代码（下载单个文件）")
    public ResponseEntity<byte[]> downloadSingle(@RequestBody GeneratorQuery query) {
        return generatorService.downloadSingle(query);
    }

    @PostMapping("/singleLocal")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "生成代码（单文件到服务器本地）")
    public void singleLocal(@RequestBody GeneratorQuery query) {
        generatorService.singleLocal(query);
    }

    @PostMapping("/template/test")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "模板测试")
    public TemplateContentVO templateTest(@RequestBody TemplateTestQuery templateTestQuery) {
        return generatorService.templateTest(templateTestQuery);
    }
}