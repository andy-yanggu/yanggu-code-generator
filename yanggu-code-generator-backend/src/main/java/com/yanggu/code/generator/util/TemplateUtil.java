package com.yanggu.code.generator.util;

import com.yanggu.code.generator.common.exception.BusinessException;
import freemarker.template.Template;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * 模板工具类
 */
@Slf4j
@UtilityClass
public class TemplateUtil {

    /**
     * 获取模板渲染后的内容
     *
     * @param templateName    模板名称
     * @param templateContent 模板内容
     * @param dataModel       数据模型
     */
    public static String renderTemplate(String templateName, String templateContent, Object dataModel) {
        try (StringReader reader = new StringReader(templateContent);
             StringWriter sw = new StringWriter()) {
            Template template = new Template(templateName, reader, null, "utf-8");
            template.process(dataModel, sw);
            return sw.toString();
        } catch (Exception e) {
            log.error("模板渲染失败，模板名称: {}，异常信息: {}", templateName, e.getMessage(), e);
            throw new BusinessException("渲染模板失败，请检查模板语法", e);
        }
    }

}
