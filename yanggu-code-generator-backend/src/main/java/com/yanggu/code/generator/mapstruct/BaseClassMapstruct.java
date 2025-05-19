package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.BaseClassDTO;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import com.yanggu.code.generator.domain.model.BaseClassModel;
import com.yanggu.code.generator.domain.vo.BaseClassVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 基类Mapstruct
 */
@Named("baseClassMapstruct")
@Mapper(componentModel = SPRING, implementationName = "GeneratorBaseClassMapstructImpl")
public interface BaseClassMapstruct extends BaseMapstruct<BaseClassEntity, BaseClassVO, BaseClassDTO> {

    BaseClassModel toModel(BaseClassEntity entity);

}