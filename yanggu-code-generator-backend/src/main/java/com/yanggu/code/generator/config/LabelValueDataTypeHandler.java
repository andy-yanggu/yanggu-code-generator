package com.yanggu.code.generator.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yanggu.code.generator.common.mybatis.typehandler.list.AbstractListTypeHandler;
import com.yanggu.code.generator.domain.entity.LabelValueDataEntity;

import java.util.List;


public class LabelValueDataTypeHandler extends AbstractListTypeHandler<LabelValueDataEntity> {

    @Override
    protected TypeReference<List<LabelValueDataEntity>> getTypeReference() {
        return new TypeReference<>() {};
    }

}
