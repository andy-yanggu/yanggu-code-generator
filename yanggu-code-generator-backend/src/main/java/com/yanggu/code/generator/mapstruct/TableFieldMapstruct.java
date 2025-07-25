package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.model.TableFieldModel;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 表字段Mapstruct
 */
@Named("tableFieldMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorTableFieldMapstructImpl")
public interface TableFieldMapstruct extends BaseMapstruct<TableFieldEntity, TableFieldVO, TableFieldDTO> {

    List<TableFieldModel> toModel(List<TableFieldEntity> entityList);

}