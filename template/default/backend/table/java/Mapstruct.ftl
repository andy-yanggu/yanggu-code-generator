package ${projectPackage}.${projectNameDot}.mapstruct;

import ${projectPackage}.${projectNameDot}.common.mapstruct.BaseMapstruct;
import ${projectPackage}.${projectNameDot}.domain.vo.${classNameUpper}VO;
import ${projectPackage}.${projectNameDot}.domain.dto.${classNameUpper}DTO;
import ${projectPackage}.${projectNameDot}.domain.entity.${classNameUpper}Entity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * ${tableComment}Mapstruct
 */
@Named("${className}Mapstruct")
@Mapper(componentModel = SPRING, implementationName = "${projectNamePascal}${classNameUpper}MapstructImpl")
public interface ${classNameUpper}Mapstruct extends BaseMapstruct<${classNameUpper}Entity, ${classNameUpper}VO, ${classNameUpper}DTO> {
}