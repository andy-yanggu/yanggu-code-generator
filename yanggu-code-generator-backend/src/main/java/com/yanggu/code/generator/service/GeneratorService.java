package com.yanggu.code.generator.service;

import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GeneratorService {

    List<PreviewTemplateVO> preview(GeneratorQuery query);

    ResponseEntity<byte[]> downloadZip(GeneratorQuery query);

    void downloadLocal(GeneratorQuery query);

    ResponseEntity<byte[]> downloadSingle(GeneratorQuery query);

    void singleLocal(GeneratorQuery query);

    TemplateContentVO templateTest(TemplateTestQuery templateTestQuery);
}