package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.codec.binary.Base64;
import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.date.DateUtil;
import cn.hutool.v7.core.io.IoUtil;
import cn.hutool.v7.core.io.file.FileUtil;
import cn.hutool.v7.core.util.CharsetUtil;
import cn.hutool.v7.core.util.EnumUtil;
import cn.hutool.v7.http.meta.HttpHeaderUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.hutool.v7.core.date.DateFormatPool.PURE_DATETIME_PATTERN;

/**
 * 生成结果下载处理器
 * 统一收口模板内容的输出：zip 打包下载、按模板类型写入本地文件、下载响应头构建。
 * DIRECTORY/TEMPLATE_FILE/BINARY_FILE 三种类型的写入分发集中于此管理。
 */
@Slf4j
@Component
public class DownloadHandler {

    /**
     * 打包为 zip 下载
     *
     * @param originFileName zip 文件名（不含时间戳与扩展名）
     * @param list           模板渲染内容列表
     * @return zip 字节流响应
     */
    public ResponseEntity<byte[]> downloadZip(String originFileName, List<TemplateContentVO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("待写入数据为空");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (TemplateContentVO templateContentVO : list) {
            try {
                // 获取文件路径
                String filePath = templateContentVO.getFilePath();

                Integer templateType = templateContentVO.getTemplateType();
                switch (EnumUtil.getBy(TemplateTypeEnum::getCode, templateType)) {
                    case DIRECTORY -> {
                        // 文件夹：创建以'/'结尾的目录条目
                        if (!filePath.endsWith("/")) {
                            filePath += "/";
                        }
                        zip.putNextEntry(new ZipEntry(filePath));
                    }
                    case TEMPLATE_FILE -> {
                        // 文件：添加条目并写入内容
                        zip.putNextEntry(new ZipEntry(filePath));
                        IoUtil.writeUtf8(zip, false, templateContentVO.getContent());
                        zip.flush();
                    }
                    case BINARY_FILE -> {
                        // 二进制文件：添加条目并写入内容
                        zip.putNextEntry(new ZipEntry(filePath));
                        IoUtil.write(zip, false, Base64.decode(templateContentVO.getContent().split(",")[1]));
                        zip.flush();
                    }
                    default -> throw new BusinessException("未知模板类型" + templateType);
                }
                zip.closeEntry();
            } catch (IOException e) {
                throw new BusinessException("模板写入失败：" + templateContentVO.getFilePath(), e);
            }
        }
        IoUtil.closeQuietly(zip);

        // zip压缩包数据
        byte[] data = outputStream.toByteArray();

        String fileName = originFileName + "_" + DateUtil.format(LocalDateTime.now(), PURE_DATETIME_PATTERN) + ".zip";

        return buildResponseEntity(fileName, data);
    }

    /**
     * 按模板类型将内容写入本地文件（覆盖方式）
     *
     * @param list 模板渲染内容列表
     */
    public void downloadLocal(List<TemplateContentVO> list) {
        if (CollUtil.isEmpty(list)) {
            log.warn("待写入数据为空");
            return;
        }
        for (TemplateContentVO templateContentVO : list) {
            Integer templateType = templateContentVO.getTemplateType();
            String filePath = templateContentVO.getFilePath();
            String content = templateContentVO.getContent();
            switch (EnumUtil.getBy(TemplateTypeEnum::getCode, templateType)) {
                //生成文件夹
                case DIRECTORY -> FileUtil.mkdir(filePath);
                //直接使用覆盖的方式生成文件
                case TEMPLATE_FILE -> FileUtil.writeUtf8String(content, filePath);
                //生成二进制文件
                case BINARY_FILE -> FileUtil.writeBytes(Base64.decode(content.split(",")[1]), filePath);
                default -> throw new BusinessException("未知模板类型：" + templateType);
            }
        }
    }

    /**
     * 构建附件下载响应
     *
     * @param fileName 下载文件名
     * @param data     文件字节数据
     * @return 附件响应
     */
    public ResponseEntity<byte[]> buildResponseEntity(String fileName, byte[] data) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpHeaderUtil.createAttachmentDisposition(fileName, CharsetUtil.UTF_8))
                .body(data);
    }
}
