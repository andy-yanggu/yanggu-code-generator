package com.yanggu.code.generator.common.mybatis.typehandler.list;

import tools.jackson.core.type.TypeReference;
import com.yanggu.code.generator.domain.entity.ModuleEntity;

import java.util.List;

public class ModuleListTypeHandler extends AbstractListTypeHandler<ModuleEntity> {

    @Override
    protected TypeReference<List<ModuleEntity>> getTypeReference() {
        return new TypeReference<>() {
        };
    }
}
