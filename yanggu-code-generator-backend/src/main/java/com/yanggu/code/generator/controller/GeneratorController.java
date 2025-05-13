package com.yanggu.code.generator.controller;

import cn.hutool.core.io.IoUtil;
import com.yanggu.code.generator.common.response.Result;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewVO;
import com.yanggu.code.generator.domain.vo.TreeVO;
import com.yanggu.code.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
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

    ///**
    // * 生成代码（zip压缩包）
    // */
    //@GetMapping("/table/downloadZip")
    //public void downloadZip(@RequestParam("tableIds") List<Long> tableIds, HttpServletResponse response) throws Exception {
    //    generatorService.downloadZip(tableIds, response);
    //}

    ///**
    // * 生成代码（本地）
    // */
    //@GetMapping("/table/downloadLocal")
    //public void downloadLocal(@RequestParam("tableId") Long tableId) {
    //    generatorService.downloadLocal(tableId);
    //}

    /**
     * 表预览代码
     */
    @PostMapping("/table/preview")
    public List<PreviewVO> tablePreview(@RequestBody GeneratorTableQuery tableQuery) {
        return generatorService.tablePreview(tableQuery);
    }

    /**
     * 表树形数据
     */
    @PostMapping("/table/treeData")
    public List<TreeVO> tableTreeData(@RequestBody GeneratorTableQuery tableQuery) {
        return generatorService.tableTreeData(tableQuery);
    }

    ///**
    // * 下载单个文件代码
    // */
    //@GetMapping("/table/download-template-content")
    //public void download(@RequestParam("tableId") Long tableId,
    //                     @RequestParam("templateId") Long templateId,
    //                     HttpServletResponse response) throws Exception {
    //    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //    TemplateInfo templateInfo = generatorService.generatorCode(tableId, templateId);
    //
    //    outputStream.write(templateInfo.getTemplateData().getBytes());
    //    byte[] data = outputStream.toByteArray();
    //
    //    response.reset();
    //    response.setHeader("Content-Disposition", "attachment; filename=" + templateInfo.getFileName());
    //    response.addHeader("Content-Length", "" + data.length);
    //    response.setContentType("application/octet-stream; charset=UTF-8");
    //
    //    IoUtil.write(response.getOutputStream(), false, data);
    //}

    ///**
    // * 下载到服务器本地
    // *
    // * @param id
    // * @return
    // * @throws IOException
    // */
    //@GetMapping("/download/{id}")
    //public Result<Void> download(@PathVariable("id") Long id) throws IOException {
    //    projectService.download(id);
    //    return Result.ok();
    //}
    //
    ///**
    // * 下载项目（压缩包）
    // */
    //@GetMapping("/download-zip/{id}")
    //public void downloadZip(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
    //    projectService.downloadZip(id, response);
    //}
    //
    ///**
    // * 预览代码
    // */
    //@GetMapping("/preview")
    //public Result<List<PreviewVO>> preview(@RequestParam("projectId") Long projectId) {
    //    List<PreviewVO> results = projectService.preview(projectId);
    //    return Result.ok(results);
    //}
    //
    ///**
    // * 树形数据
    // */
    //@GetMapping("/tree-data")
    //public Result<List<TreeVO>> treeData(@RequestParam("projectId") Long projectId) {
    //    List<TreeVO> results = projectService.treeData(projectId);
    //    return Result.ok(results);
    //}
    //
    ///**
    // * 下载单个文件代码
    // */
    //@GetMapping("/download-template-content")
    //public void download(@RequestParam("projectId") Long projectId,
    //                     @RequestParam("templateId") Long templateId,
    //                     @RequestParam("templateGroupType") Integer templateGroupType,
    //                     @RequestParam(value = "tableId", required = false) Long tableId,
    //                     HttpServletResponse response) throws Exception {
    //    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //    TemplateInfo templateInfo = projectService.generatorCode(projectId, tableId, templateId, templateGroupType);
    //
    //    outputStream.write(templateInfo.getTemplateData().getBytes());
    //    byte[] data = outputStream.toByteArray();
    //
    //    response.reset();
    //    response.setHeader("Content-Disposition", "attachment; filename=" + templateInfo.getFileName());
    //    response.addHeader("Content-Length", "" + data.length);
    //    response.setContentType("application/octet-stream; charset=UTF-8");
    //
    //    IoUtil.write(response.getOutputStream(), false, data);
    //}

}