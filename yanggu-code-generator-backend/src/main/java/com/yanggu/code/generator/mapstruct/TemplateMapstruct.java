package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.common.mapstruct.EntityToDTOMapstruct;
import com.yanggu.code.generator.domain.bo.TemplateBO;
import com.yanggu.code.generator.domain.dto.TemplateDTO;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 模板Mapstruct
 */
@Named("templateMapstruct")
@Mapper(componentModel = SPRING, implementationName = "GeneratorTemplateMapstructImpl")
public interface TemplateMapstruct extends BaseMapstruct<TemplateEntity, TemplateVO, TemplateDTO> {

    TemplateDTO boToDTO(TemplateBO bo);

}