package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.ProjectDTO;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.vo.ProjectVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 项目Mapstruct
 */
@Named("projectMapstruct")
@Mapper(componentModel = SPRING, implementationName = "CodeGeneratorProjectMapstructImpl")
public interface ProjectMapstruct extends BaseMapstruct<ProjectEntity, ProjectVO, ProjectDTO> {
}