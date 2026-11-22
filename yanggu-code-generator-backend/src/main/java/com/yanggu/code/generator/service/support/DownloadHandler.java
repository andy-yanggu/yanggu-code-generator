package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.codec.binary.Base64;
import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.date.DateUtil;
import cn.hutool.v7.core.io.IoUtil;
import cn.hutool.v7.core.io.file.FileUtil;
import cn.hutool.v7.core.text.StrUtil;
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
        // 使用 try-with-resources 保证异常路径下 zip 也被关闭（写入中央目录并刷新到 outputStream）
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
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
                            IoUtil.write(zip, false, extractBase64(templateContentVO.getContent()));
                            zip.flush();
                        }
                        default -> throw new BusinessException("未知模板类型" + templateType);
                    }
                    zip.closeEntry();
                } catch (IOException e) {
                    throw new BusinessException("模板写入失败：" + templateContentVO.getFilePath(), e);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("模板打包失败", e);
        }

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
                case BINARY_FILE -> FileUtil.writeBytes(extractBase64(content), filePath);
                default -> throw new BusinessException("未知模板类型：" + templateType);
            }
        }
    }

    /**
     * 从 Data URL 中安全提取并解码 Base64 内容
     * <p>Data URL 形如 {@code data:<mediaType>;base64,<data>}，取首个逗号之后的部分解码</p>
     * <p>内容为空或不含逗号（非合法 Data URL）时快速失败，避免数组越界与空指针</p>
     *
     * @param dataUrl 二进制模板的 Data URL 内容
     * @return 解码后的字节数组
     */
    private static byte[] extractBase64(String dataUrl) {
        if (StrUtil.isBlank(dataUrl)) {
            throw new BusinessException("二进制模板内容为空");
        }
        int commaIndex = dataUrl.indexOf(',');
        if (commaIndex < 0) {
            throw new BusinessException("二进制模板内容不是合法的 Data URL：" + dataUrl.substring(0, Math.min(dataUrl.length(), 50)));
        }
        return Base64.decode(dataUrl.substring(commaIndex + 1));
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
