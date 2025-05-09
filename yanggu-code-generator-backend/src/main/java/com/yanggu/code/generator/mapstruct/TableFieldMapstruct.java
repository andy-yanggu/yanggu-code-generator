package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 表字段Mapstruct
 */
@Named("tableFieldMapstruct")
@Mapper(componentModel = SPRING, implementationName = "GeneratorTableFieldMapstructImpl")
public interface TableFieldMapstruct extends BaseMapstruct<TableFieldEntity, TableFieldVO, TableFieldDTO> {
}