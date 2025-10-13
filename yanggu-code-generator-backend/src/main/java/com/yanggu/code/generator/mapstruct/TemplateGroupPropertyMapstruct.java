package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 模板组属性Mapstruct
 */
@Named("templateGroupPropertyMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorGenTemplateGroupPropertyMapstructImpl")
public interface TemplateGroupPropertyMapstruct extends BaseMapstruct<TemplateGroupPropertyEntity, TemplateGroupPropertyVO, TemplateGroupPropertyDTO> {
}