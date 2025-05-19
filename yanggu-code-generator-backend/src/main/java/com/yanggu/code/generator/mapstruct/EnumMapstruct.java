package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.EnumDTO;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.vo.EnumVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 枚举Mapstruct
 */
@Named("enumMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorEnumMapstructImpl")
public interface EnumMapstruct extends BaseMapstruct<EnumEntity, EnumVO, EnumDTO> {
}