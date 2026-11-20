package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * render 单模板渲染测试：类型直传 Integer，不依赖模板组实体
 */
class PreviewBuilderRenderTest {

    private final PreviewBuilder previewBuilder = new PreviewBuilder();

    private TemplateVO template(Integer templateType, String content) {
        TemplateVO template = new TemplateVO();
        template.setId(11L);
        template.setTemplateName("demo.ftl");
        template.setTemplateType(templateType);
        template.setTemplateContent(content);
        template.setGeneratorPath("src/main/${name}.txt");
        return template;
    }

    @Test
    void renderTemplateFilePassesGroupTypeThrough() {
        TemplateVO template = template(1, "hello ${name}");
        TemplateContentVO vo = previewBuilder.render(2, template, Map.of("name", "world"));
        assertEquals("hello world", vo.getContent());
        assertEquals("world.txt", vo.getFileName());
        assertEquals("src/main/world.txt", vo.getFilePath());
        assertEquals(11L, vo.getTemplateId());
        assertEquals(2, vo.getTemplateGroupType());
        assertEquals(1, vo.getTemplateType());
    }

    @Test
    void renderDirectoryProducesEmptyContent() {
        TemplateVO template = template(0, null);
        TemplateContentVO vo = previewBuilder.render(0, template, Map.of("name", "demo"));
        assertEquals("", vo.getContent());
        assertEquals("demo.txt", vo.getFileName());
        assertEquals(0, vo.getTemplateGroupType());
    }
}