package com.yanggu.code.generator.util;

import cn.hutool.v7.core.text.StrUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import freemarker.core.InvalidReferenceException;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
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

    private static final Configuration cfg;

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        // 不要千分位，从根源解决
        cfg.setNumberFormat("0");
        // 沙箱：禁止模板通过 ?new 实例化任意 Java 类（默认 UNRESTRICTED_RESOLVER 可用 Execute 类执行命令），
        // 用户可编辑的模板内容不可信，一律拒绝内建类解析
        cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
    }

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
            Template template = new Template(templateName, reader, cfg, "utf-8");
            template.process(dataModel, sw);
            return sw.toString();
        } catch (Exception e) {
            log.error("模板渲染失败，模板名称: {}，异常信息: {}", templateName, e.getMessage(), e);
            if (e instanceof InvalidReferenceException e2) {
                String format = StrUtil.format("{}模板文件渲染异常，请查看异常信息：{}", templateName, e2.getMessageWithoutStackTop());
                throw new BusinessException(format);
            } else {
                throw new BusinessException(e.getMessage());
            }
        }
    }

}
