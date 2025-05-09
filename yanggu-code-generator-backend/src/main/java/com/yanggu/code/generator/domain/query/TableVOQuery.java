package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.TableVO;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 表VO查询实体类
 */
@Data
@Schema(description = "表VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TableVOQuery extends PageQuery<TableVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}