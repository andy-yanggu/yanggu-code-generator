package com.yanggu.code.generator.service;


import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewVO;
import com.yanggu.code.generator.domain.vo.TreeVO;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface GeneratorService {

    List<PreviewVO> tablePreview(GeneratorTableQuery tableQuery);

    List<TreeVO> tableTreeData(GeneratorTableQuery tableQuery);

    ResponseEntity<byte[]> tableDownloadTemplateContent(Long tableId, Long templateId) throws IOException;

    ResponseEntity<byte[]> tableBatchDownloadZip(List<Long> tableIds) throws IOException;

    void tableDownloadLocal(GeneratorTableQuery tableQuery);

    ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) throws IOException;

}
