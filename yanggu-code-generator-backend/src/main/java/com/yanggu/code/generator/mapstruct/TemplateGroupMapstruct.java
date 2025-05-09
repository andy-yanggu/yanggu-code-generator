package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 模板组Mapstruct
 */
@Named("templateGroupMapstruct")
@Mapper(componentModel = SPRING, implementationName = "GeneratorTemplateGroupMapstructImpl")
public interface TemplateGroupMapstruct extends BaseMapstruct<TemplateGroupEntity, TemplateGroupVO, TemplateGroupDTO> {
}