package com.yanggu.code.generator.config;

import cn.hutool.v7.core.collection.CollUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yanggu.code.generator.common.mybatis.typehandler.list.AbstractListTypeHandler;
import com.yanggu.code.generator.domain.entity.LabelValueDataEntity;
import com.yanggu.code.generator.util.GenUtil;

import java.util.List;


public class LabelValueDataTypeHandler extends AbstractListTypeHandler<LabelValueDataEntity> {

    @Override
    protected List<LabelValueDataEntity> toObject(Object o) {
        List<LabelValueDataEntity> list = super.toObject(o);
        if (CollUtil.isEmpty(list)) {
            return list;
        } else {
            for (LabelValueDataEntity labelValueDataEntity : list) {
                labelValueDataEntity.setValue(GenUtil.handleData(labelValueDataEntity.getValue().toString()));
            }
            return list;
        }
    }

    @Override
    protected TypeReference<List<LabelValueDataEntity>> getTypeReference() {
        return new TypeReference<>() {
        };
    }

}
