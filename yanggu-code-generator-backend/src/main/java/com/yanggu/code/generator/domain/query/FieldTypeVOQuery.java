package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.FieldTypeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字段类型VO查询实体类
 */
@Data
@Schema(description = "字段类型VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class FieldTypeVOQuery extends PageQuery<FieldTypeVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}