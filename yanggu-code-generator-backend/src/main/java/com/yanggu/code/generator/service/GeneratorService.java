package com.yanggu.code.generator.service;


import com.yanggu.code.generator.domain.query.*;
import com.yanggu.code.generator.domain.vo.PreviewDataVO;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface GeneratorService {

    List<PreviewTemplateVO> preview(CodePreviewQuery codePreviewQuery) throws Exception;

    ResponseEntity<byte[]> downloadSingle(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception;

    void singleLocal(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception;

    void projectDownloadLocal(GeneratorProjectQuery projectQuery) throws Exception;

    ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception;

    void tableDownloadLocal(GeneratorTableQuery tableQuery);

    ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) throws IOException;

    void enumDownloadLocal(GeneratorEnumQuery enumQuery);

    ResponseEntity<byte[]> enumDownloadZip(GeneratorEnumQuery enumQuery);

}
