package com.yanggu.code.generator.util;

import com.yanggu.code.generator.common.exception.BusinessException;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.io.IoUtil;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * 模板工具类
 */
@Slf4j
public class TemplateUtils {

    /**
     * 获取模板渲染后的内容
     *
     * @param content   模板内容
     * @param dataModel 数据模型
     */
    public static String getContent(String content, String templateName, Object dataModel) {
        StringReader reader = new StringReader(content);
        StringWriter sw = new StringWriter();
        try {
            // 渲染模板
            Template template = new Template(templateName, reader, null, "utf-8");
            template.process(dataModel, sw);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("渲染模板失败，请检查模板语法", e);
        }

        content = sw.toString();

        IoUtil.closeQuietly(reader);
        IoUtil.closeQuietly(sw);

        return content;
    }

}
