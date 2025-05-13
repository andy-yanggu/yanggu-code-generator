package com.yanggu.code.generator.service;


import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewVO;
import com.yanggu.code.generator.domain.vo.TreeVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface GeneratorService {

    List<PreviewVO> tablePreview(GeneratorTableQuery tableQuery);

    List<TreeVO> tableTreeData(GeneratorTableQuery tableQuery);

    void tableDownloadTemplateContent(Long tableId, Long templateId, HttpServletResponse response) throws IOException;

    void tableDownloadZip(List<Long> tableIds, HttpServletResponse response) throws IOException;

    void tableDownloadLocal(GeneratorTableQuery tableQuery);

}
