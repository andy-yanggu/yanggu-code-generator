package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.DatasourceVO;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据源VO查询实体类
 */
@Data
@Schema(description = "数据源VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class DatasourceVOQuery extends PageQuery<DatasourceVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}