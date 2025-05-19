package com.yanggu.code.generator.service;


import com.yanggu.code.generator.domain.query.GeneratorProjectQuery;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewDataVO;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface GeneratorService {

    PreviewDataVO tablePreview(Long tableId);

    ResponseEntity<byte[]> tableDownloadSingle(Long tableId, Long templateId) throws IOException;

    void tableDownloadLocal(GeneratorTableQuery tableQuery);

    ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) throws IOException;

    PreviewDataVO projectPreview(Long projectId) throws Exception;

    void projectDownloadLocal(GeneratorProjectQuery projectQuery) throws Exception;

    ResponseEntity<byte[]> projectDownloadSingle(Integer templateGroupType, Long id, Long templateId) throws Exception;

    ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception;

    PreviewDataVO enumPreview(Long enumId);

}
