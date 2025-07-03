package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.FieldTypeDTO;
import com.yanggu.code.generator.domain.entity.FieldTypeEntity;
import com.yanggu.code.generator.domain.vo.FieldTypeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 字段类型Mapstruct
 */
@Named("fieldTypeMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorFieldTypeMapstructImpl")
public interface FieldTypeMapstruct extends BaseMapstruct<FieldTypeEntity, FieldTypeVO, FieldTypeDTO> {
}