package com.yanggu.code.generator.mapstruct;

import com.yanggu.code.generator.common.mapstruct.BaseMapstruct;
import com.yanggu.code.generator.domain.dto.DatasourceDTO;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.vo.DatasourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * 数据源Mapstruct
 */
@Named("datasourceMapstruct")
@Mapper(componentModel = SPRING, implementationName = "GeneratorDatasourceMapstructImpl")
public interface DatasourceMapstruct extends BaseMapstruct<DatasourceEntity, DatasourceVO, DatasourceDTO> {
}