package com.yanggu.code.generator.service;


import com.yanggu.code.generator.domain.query.GeneratorEnumQuery;
import com.yanggu.code.generator.domain.query.GeneratorProjectQuery;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewDataVO;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface GeneratorService {

    PreviewDataVO projectPreview(Long projectId) throws Exception;

    void projectDownloadLocal(GeneratorProjectQuery projectQuery) throws Exception;

    ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception;

    ResponseEntity<byte[]> projectDownloadSingle(Integer templateGroupType, Long id, Long templateId) throws Exception;

    PreviewDataVO tablePreview(Long tableId);

    void tableDownloadLocal(GeneratorTableQuery tableQuery);

    ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) throws IOException;

    ResponseEntity<byte[]> tableDownloadSingle(Long tableId, Long templateId) throws IOException;

    PreviewDataVO enumPreview(Long enumId);

    void enumDownloadLocal(GeneratorEnumQuery enumQuery);

    ResponseEntity<byte[]> enumDownloadZip(GeneratorEnumQuery enumQuery);

    ResponseEntity<byte[]> enumDownloadSingle(Long enumId, Long templateId);

}
