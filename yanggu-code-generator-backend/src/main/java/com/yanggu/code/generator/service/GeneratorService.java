package com.yanggu.code.generator.service;


import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewVO;
import com.yanggu.code.generator.domain.vo.TreeVO;

import java.util.List;

public interface GeneratorService {

    List<PreviewVO> tablePreview(GeneratorTableQuery tableQuery);

    List<TreeVO> tableTreeData(GeneratorTableQuery tableQuery);

}
