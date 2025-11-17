package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.bo.TemplateGroupBO;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 模板组Mapstruct
 */
@Named("templateGroupMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorTemplateGroupMapstructImpl", uses = {TemplateGroupPropertyMapstruct.class})
public interface TemplateGroupMapstruct extends BaseMapstruct<TemplateGroupEntity, TemplateGroupVO, TemplateGroupDTO> {

    @Override
    @Mapping(source = "propertyList", target = "propertyList", qualifiedByName = {"templateGroupPropertyMapstruct", "entityToVO"})
    TemplateGroupVO entityToVO(TemplateGroupEntity entity);

    List<TemplateGroupBO> entityToBO(List<TemplateGroupEntity> entityList);

    TemplateGroupDTO boToDTO(TemplateGroupBO bo);

}