package com.yanggu.code.generator.service;

import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GeneratorService {

    List<PreviewTemplateVO> preview(GeneratorQuery query);

    ResponseEntity<byte[]> downloadZip(GeneratorQuery query);

    void downloadLocal(GeneratorQuery query);

    ResponseEntity<byte[]> downloadSingle(GeneratorQuery query);

    void singleLocal(GeneratorQuery query);

    TemplateContentVO templateTest(TemplateTestQuery templateTestQuery);

    /**
     * 模板测试带值变量目录：按选中测试对象构建数据模型实例，合成每个变量的实际值（敏感字段已掩码）
     */
    List<VariableNodeVO> testDataModel(TemplateTestQuery templateTestQuery);
}