package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.TableDTO;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.vo.TableVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 表Mapstruct
 */
@Named("tableMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorTableMapstructImpl")
public interface TableMapstruct extends BaseMapstruct<TableEntity, TableVO, TableDTO> {
}