package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.EnumItemDTO;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.vo.EnumItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 枚举项Mapstruct
 */
@Named("enumItemMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorEnumItemMapstructImpl")
public interface EnumItemMapstruct extends BaseMapstruct<EnumItemEntity, EnumItemVO, EnumItemDTO> {
}